package com.project.practice.sap.service;

import com.project.practice.sap.dto.ApproveVersionRequest;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.exception.IllegalStatusException;
import com.project.practice.sap.exception.ResourceNotFoundException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.DocumentStatus;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.repository.VersionRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final DtoMapper dtoMapper;

    public VersionServiceImpl(VersionRepository versionRepository,
                              DocumentRepository documentRepository,
                              UserRepository userRepository,
                              FileStorageService fileStorageService,
                              DtoMapper dtoMapper) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.dtoMapper = dtoMapper;
    }

    @Override
    @Transactional
    public VersionResponseDTO uploadNewVersion(Integer documentId, Integer userId, MultipartFile file) {
        fileStorageService.validateTxtFile(file);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (versionRepository.existsByDocumentIdAndStatus(documentId, DocumentStatus.UNDER_REVIEW)) {
            throw new IllegalStatusException(
                    "A version is already pending review for this document. " +
                    "It must be approved or rejected before a new version can be uploaded.");
        }

        int nextVersionNum = versionRepository.countByDocumentId(documentId) + 1;
        String filePath = fileStorageService.saveFileToDisk(file, documentId, String.valueOf(nextVersionNum));

        Version version = new Version();
        version.setDocument(document);
        version.setCreatedBy(user);
        version.setVersionNum(String.valueOf(nextVersionNum));
        version.setStatus(DocumentStatus.UNDER_REVIEW);
        version.setActive(false);
        version.setFilePath(filePath);

        return dtoMapper.toVersionDTO(versionRepository.save(version));
    }

    @Override
    public List<VersionResponseDTO> getVersionHistory(Integer documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }
        return versionRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(version -> dtoMapper.toVersionDTO(version))
                .toList();
    }

    @Override
    public VersionResponseDTO getVersion(Integer documentId, Integer versionId) {
        return dtoMapper.toVersionDTO(findVersionForDocument(documentId, versionId));
    }

    @Override
    public VersionResponseDTO getActiveVersion(Integer documentId) {
        return versionRepository.findByDocumentIdAndIsActiveTrue(documentId)
                .map(version -> dtoMapper.toVersionDTO(version))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active (approved) version found for document id: " + documentId));
    }

    @Override
    public Resource downloadFile(Integer documentId, Integer versionId) {
        Version version = findVersionForDocument(documentId, versionId);
        return fileStorageService.loadFileAsResource(version.getFilePath());
    }

    @Override
    @Transactional
    public VersionResponseDTO approveVersion(Integer documentId, Integer versionId, ApproveVersionRequest request) {
        Version version = findVersionForDocument(documentId, versionId);

        if (version.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new IllegalStatusException(
                    "Cannot approve version with status: " + version.getStatus() +
                    ". Only UNDER_REVIEW versions can be approved.");
        }

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + request.reviewerId()));

        versionRepository.findByDocumentIdAndIsActiveTrue(documentId).ifPresent(activeVersion -> {
            activeVersion.setActive(false);
            activeVersion.setStatus(DocumentStatus.ARCHIVED);
            versionRepository.save(activeVersion);
        });

        version.setStatus(DocumentStatus.APPROVED);
        version.setActive(true);
        version.setReviewedBy(reviewer);
        version.setReviewComment(request.comment());

        return dtoMapper.toVersionDTO(versionRepository.save(version));
    }

    @Override
    @Transactional
    public VersionResponseDTO rejectVersion(Integer documentId, Integer versionId, ApproveVersionRequest request) {
        Version version = findVersionForDocument(documentId, versionId);

        if (version.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new IllegalStatusException(
                    "Cannot reject version with status: " + version.getStatus() +
                    ". Only UNDER_REVIEW versions can be rejected.");
        }

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + request.reviewerId()));

        version.setStatus(DocumentStatus.REJECTED);
        version.setReviewedBy(reviewer);
        version.setReviewComment(request.comment());

        return dtoMapper.toVersionDTO(versionRepository.save(version));
    }

    // Finds a version and verifies it belongs to the given document
    private Version findVersionForDocument(Integer documentId, Integer versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + versionId));
        if (!version.getDocument().getId().equals(documentId)) {
            throw new ResourceNotFoundException(
                    "Version " + versionId + " does not belong to document " + documentId);
        }
        return version;
    }
}
