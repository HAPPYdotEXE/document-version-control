package com.project.practice.sap.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.practice.sap.dto.CreateDocumentRequestDTO;
import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.exception.ForbiddenOperationException;
import com.project.practice.sap.exception.NotFoundException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.model.enums.VersionStatus;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.repository.VersionRepository;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final VersionRepository versionRepository;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            VersionRepository versionRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.versionRepository = versionRepository;
    }

    @Override
    public DocumentResponseDTO createDocument(CreateDocumentRequestDTO request) {
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new NotFoundException("Author not found"));

        if (!author.hasRole(RoleType.AUTHOR) && !author.hasRole(RoleType.ADMIN)) {
            throw new ForbiddenOperationException("User does not have permission to create documents");
        }

        Document document = new Document();
        document.setTitle(request.title());
        document.setDescription(request.description());
        document.setAuthor(author);

        Document savedDocument = documentRepository.save(document);

        Version firstVersion = new Version();
        firstVersion.setDocument(savedDocument);
        firstVersion.setVersionNumber(1);
        firstVersion.setContent(request.initialContent());
        firstVersion.setChangeSummary(request.changeSummary());
        firstVersion.setStatus(VersionStatus.APPROVED);
        firstVersion.setCreatedBy(author);

        Version savedVersion = versionRepository.save(firstVersion);

        savedDocument.setActiveVersion(savedVersion);
        Document finalDocument = documentRepository.save(savedDocument);

        return toResponse(finalDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponseDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        return toResponse(document);
    }

    private DocumentResponseDTO toResponse(Document document) {
        Long activeVersionId = null;
        Integer activeVersionNumber = null;

        if (document.getActiveVersion() != null) {
            activeVersionId = document.getActiveVersion().getId();
            activeVersionNumber = document.getActiveVersion().getVersionNumber();
        }

        return new DocumentResponseDTO(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getAuthor().getUsername(),
                activeVersionId,
                activeVersionNumber,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
