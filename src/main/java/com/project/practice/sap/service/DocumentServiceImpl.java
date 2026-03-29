package com.project.practice.sap.service;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.exception.DuplicateResourceException;
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

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final VersionRepository versionRepository;
    private final FileStorageService fileStorageService;
    private final DtoMapper dtoMapper;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               UserRepository userRepository,
                               VersionRepository versionRepository,
                               FileStorageService fileStorageService,
                               DtoMapper dtoMapper) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.versionRepository = versionRepository;
        this.fileStorageService = fileStorageService;
        this.dtoMapper = dtoMapper;
    }

    @Override
    @Transactional
    public DocumentResponseDTO createDocument(String name, Integer userId, MultipartFile file) {
        fileStorageService.validateTxtFile(file);

        if (documentRepository.existsByName(name)) {
            throw new DuplicateResourceException("A document with name '" + name + "' already exists.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Document document = new Document();
        document.setName(name);
        document.setCreatedBy(user);
        Document savedDocument = documentRepository.save(document);

        String filePath = fileStorageService.saveFileToDisk(file, savedDocument.getId(), 1);

        Version version = new Version();
        version.setDocument(savedDocument);
        version.setCreatedBy(user);
        version.setVersionNum(1);
        version.setStatus(DocumentStatus.UNDER_REVIEW);
        version.setActive(false);
        version.setFilePath(filePath);
        versionRepository.save(version);

        return dtoMapper.toDocumentDTO(savedDocument);
    }

    @Override
    public DocumentResponseDTO getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return dtoMapper.toDocumentDTO(document);
    }

    @Override
    public List<DocumentResponseDTO> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(dtoMapper::toDocumentDTO)
                .toList();
    }
}
