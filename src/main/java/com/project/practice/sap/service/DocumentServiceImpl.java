package com.project.practice.sap.service;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.exception.DuplicateResourceException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.repository.VersionRepository;
import com.project.practice.sap.service.util.DtoMapper;
import com.project.practice.sap.service.util.EntityBuilder;
import com.project.practice.sap.service.util.EntityLookup;
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
    private final EntityLookup entityLookup;
    private final EntityBuilder entityBuilder;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               UserRepository userRepository,
                               VersionRepository versionRepository,
                               FileStorageService fileStorageService,
                               DtoMapper dtoMapper,
                               EntityLookup entityLookup,
                               EntityBuilder entityBuilder) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.versionRepository = versionRepository;
        this.fileStorageService = fileStorageService;
        this.dtoMapper = dtoMapper;
        this.entityLookup = entityLookup;
        this.entityBuilder = entityBuilder;
    }

    @Override
    @Transactional
    public DocumentResponseDTO createDocument(String name, Integer userId, MultipartFile file) {
        fileStorageService.validateTxtFile(file);

        if (documentRepository.existsByName(name)) {
            throw new DuplicateResourceException("A document with name '" + name + "' already exists.");
        }

        User user = entityLookup.findUserById(userId);

        Document savedDocument = documentRepository.save(entityBuilder.buildDocument(name, user));
        String filePath = fileStorageService.saveFileToDisk(file, savedDocument.getId(), 1);
        versionRepository.save(entityBuilder.buildVersion(savedDocument, user, 1, filePath));

        return dtoMapper.toDocumentDTO(savedDocument);
    }

    @Override
    public DocumentResponseDTO getDocumentById(Integer id) {
        return dtoMapper.toDocumentDTO(entityLookup.findDocumentById(id));
    }

    @Override
    public List<DocumentResponseDTO> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(dtoMapper::toDocumentDTO)
                .toList();
    }

    @Override
    @Transactional
    public DocumentResponseDTO updateDocument(Integer id, String name) {
        Document document = entityLookup.findDocumentById(id);

        if (!document.getName().equals(name) && documentRepository.existsByName(name)) {
            throw new DuplicateResourceException("A document with name '" + name + "' already exists.");
        }
        document.setName(name);
        return dtoMapper.toDocumentDTO(documentRepository.save(document));
    }

    @Override
    @Transactional
    public void deleteDocument(Integer id) {
        Document document = entityLookup.findDocumentById(id);

        List<Version> versions = versionRepository.findByDocumentIdOrderByCreatedAtAsc(id);
        // deleting the files from the server then removing the entities from the DB
        for (Version version : versions) {
            fileStorageService.deleteFile(version.getFilePath());
            versionRepository.deleteById(version.getId());
        }
        documentRepository.delete(document);
    }
}
