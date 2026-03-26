package com.project.practice.sap.service;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.dto.UserSummaryDTO;
import com.project.practice.sap.exception.DuplicateResourceException;
import com.project.practice.sap.exception.InvalidFileException;
import com.project.practice.sap.exception.ResourceNotFoundException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.DocumentStatus;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.repository.VersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final VersionRepository versionRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               UserRepository userRepository,
                               VersionRepository versionRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.versionRepository = versionRepository;
    }

    @Override
    @Transactional
    public DocumentResponseDTO createDocument(String name, Integer userId, MultipartFile file) {

        validateTxtFile(file);

        if (documentRepository.existsByName(name)) {
            throw new DuplicateResourceException("A document with name '" + name + "' already exists.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Document document = new Document();
        document.setName(name);
        document.setCreatedBy(user);
        Document savedDocument = documentRepository.save(document);

        // Build the file path using the document's ID, then save to disk
        String filePath = saveFileToDisk(file, savedDocument.getId(), "1");

        // Create Version 1 — UNDER_REVIEW, not yet active (requires approval to become active)
        Version version = new Version();
        version.setDocument(savedDocument);
        version.setCreatedBy(user);
        version.setVersionNum("1");
        version.setStatus(DocumentStatus.UNDER_REVIEW);
        version.setActive(false);
        version.setFilePath(filePath);
        versionRepository.save(version);

        return toDTO(savedDocument);
    }

    @Override
    public DocumentResponseDTO getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return toDTO(document);
    }

    @Override
    public List<DocumentResponseDTO> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(document -> toDTO(document))
                .toList();
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

    private DocumentResponseDTO toDTO(Document document) {
        UserSummaryDTO createdBy = new UserSummaryDTO(
                document.getCreatedBy().getId(),
                document.getCreatedBy().getUsername()
        );
        return new DocumentResponseDTO(
                document.getId(),
                document.getName(),
                document.getCreatedAt(),
                createdBy
        );
    }
}
