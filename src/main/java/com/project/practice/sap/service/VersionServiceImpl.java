package com.project.practice.sap.service;

import com.project.practice.sap.dto.ApproveVersionRequest;
import com.project.practice.sap.dto.UserSummaryDTO;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.exception.InvalidFileException;
import com.project.practice.sap.exception.ResourceNotFoundException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.DocumentStatus;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.repository.VersionRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public VersionServiceImpl(VersionRepository versionRepository,
                               DocumentRepository documentRepository,
                               UserRepository userRepository) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public VersionResponseDTO uploadNewVersion(Integer documentId, Integer userId, MultipartFile file) {
        validateTxtFile(file);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // only one version can be pending review at a time
        if (versionRepository.existsByDocumentIdAndStatus(documentId, DocumentStatus.UNDER_REVIEW)) {
            throw new IllegalStateException(
                    "A version is already pending review for this document. " +
                    "It must be approved or rejected before a new version can be uploaded.");
        }

        // Calculate next version number: count existing versions + 1
        int nextVersionNum = versionRepository.countByDocumentId(documentId) + 1;

        String filePath = saveFileToDisk(file, documentId, String.valueOf(nextVersionNum));

        Version version = new Version();
        version.setDocument(document);
        version.setCreatedBy(user);
        version.setVersionNum(String.valueOf(nextVersionNum));
        version.setStatus(DocumentStatus.UNDER_REVIEW);
        version.setActive(false);
        version.setFilePath(filePath);

        return toDTO(versionRepository.save(version));
    }

    @Override
    public List<VersionResponseDTO> getVersionHistory(Integer documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }

        return versionRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(version -> toDTO(version))
                .toList();
    }

    @Override
    public VersionResponseDTO getVersion(Integer documentId, Integer versionId){
        Version version = findVersionForDocument(documentId, versionId);
        return toDTO(version);
    }

    @Override
    public VersionResponseDTO getActiveVersion(Integer documentId) {
        return versionRepository.findByDocumentIdAndIsActiveTrue(documentId)
                .map(version -> toDTO(version))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active (approved) version found for document id: " + documentId));
    }

    @Override
    public Resource downloadFile(Integer documentId, Integer versionId) {
        Version version = findVersionForDocument(documentId, versionId);

        // checking if documentId matches versionId for now --> could change it to match front end once we get to building it
        if (!version.getDocument().getId().equals(documentId)) {
            throw new ResourceNotFoundException("Version " + versionId + " does not belong to document " + documentId);
        }

        try {
            Path filePath = Path.of(version.getFilePath()).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or is damaged: " + filePath);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not resolve file path: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public VersionResponseDTO approveVersion(Integer documentId, Integer versionId, ApproveVersionRequest request) {
        Version version = findVersionForDocument(documentId, versionId);

        if (version.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new RuntimeException(
                    "Cannot approve version with status: " + version.getStatus() +
                    ". Only UNDER_REVIEW versions can be approved.");
        }

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + request.reviewerId()));

        // deactivate and archive the curr active version
        versionRepository.findByDocumentIdAndIsActiveTrue(documentId).ifPresent(activeVersion -> {
            activeVersion.setActive(false);
            activeVersion.setStatus(DocumentStatus.ARCHIVED);
            versionRepository.save(activeVersion);
        });

        // activate new version
        version.setStatus(DocumentStatus.APPROVED);
        version.setActive(true);
        version.setReviewedBy(reviewer);
        version.setReviewComment(request.comment());

        return toDTO(versionRepository.save(version));
    }

    @Override
    @Transactional
    public VersionResponseDTO rejectVersion(Integer documentId, Integer versionId, ApproveVersionRequest request) {
        Version version = findVersionForDocument(documentId, versionId);

        if (version.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new RuntimeException(
                    "Cannot reject version with status: " + version.getStatus() +
                    ". Only UNDER_REVIEW versions can be rejected.");
        }

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + request.reviewerId()));

        version.setStatus(DocumentStatus.REJECTED);
        version.setReviewedBy(reviewer);
        version.setReviewComment(request.comment());

        return toDTO(versionRepository.save(version));
    }

    // Finds a version and verifies it belongs to the given document
    private Version findVersionForDocument(Integer documentId, Integer versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + versionId));

        if (!version.getDocument().getId().equals(documentId)) {
            throw new ResourceNotFoundException("Version " + versionId + " does not belong to document " + documentId);
        }
        return version;
    }

    private void validateTxtFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".txt")) {
            throw new InvalidFileException("Only .txt files are accepted. Received: " + originalName);
        }
    }

    private String saveFileToDisk(MultipartFile file, Integer documentId, String versionNum) {
        try {
            // create the file path then transfer the file here
            Path directory = Files.createDirectories(Path.of("uploads", "documents", String.valueOf(documentId)));
            Path filePath = directory.resolve(versionNum + ".txt");

            file.transferTo(filePath);
            return filePath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file to disk: " + e.getMessage(), e);
        }
    }

    private UserSummaryDTO toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummaryDTO(user.getId(), user.getUsername());
    }

    private VersionResponseDTO toDTO(Version version) {
        return new VersionResponseDTO(
                version.getId(),
                version.getVersionNum(),
                version.getStatus(),
                version.isActive(),
                version.getFilePath(),
                version.getCreatedAt(),
                toUserSummary(version.getCreatedBy()),
                toUserSummary(version.getReviewedBy()),   // null until reviewed
                version.getReviewComment(),                // null until reviewed
                version.getDocument().getId()
        );
    }
}
