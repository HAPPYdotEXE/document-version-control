package com.project.practice.sap.service;

import com.project.practice.sap.dto.DocumentViewDTO;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.exception.IllegalStatusException;
import com.project.practice.sap.exception.ResourceNotFoundException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.AuditAction;
import com.project.practice.sap.model.enums.AuditEntityType;
import com.project.practice.sap.model.enums.DocumentStatus;
import com.project.practice.sap.repository.VersionRepository;
import com.project.practice.sap.service.util.DtoMapper;
import com.project.practice.sap.service.util.EntityBuilder;
import com.project.practice.sap.service.util.EntityLookup;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;
    private final FileStorageService fileStorageService;
    private final DtoMapper dtoMapper;
    private final EntityLookup entityLookup;
    private final EntityBuilder entityBuilder;
    private final AuditLogService auditLogService;

    public VersionServiceImpl(VersionRepository versionRepository,
                              FileStorageService fileStorageService,
                              DtoMapper dtoMapper,
                              EntityLookup entityLookup,
                              EntityBuilder entityBuilder,
                              AuditLogService auditLogService) {
        this.versionRepository = versionRepository;
        this.fileStorageService = fileStorageService;
        this.dtoMapper = dtoMapper;
        this.entityLookup = entityLookup;
        this.entityBuilder = entityBuilder;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public VersionResponseDTO uploadNewVersion(Integer documentId, MultipartFile file) {
        fileStorageService.validateTxtFile(file);

        Document document = entityLookup.findDocumentById(documentId);
        User user = entityLookup.getCurrentUser();

        if (versionRepository.existsByDocumentIdAndStatus(documentId, DocumentStatus.UNDER_REVIEW)) {
            throw new IllegalStatusException(
                    "A version is already pending review for this document. " +
                    "It must be approved or rejected before a new version can be uploaded.");
        }

        int nextVersionNum = versionRepository.countByDocumentId(documentId) + 1;
        String filePath = fileStorageService.saveFileToDisk(file, documentId, nextVersionNum);

        Version version = versionRepository.save(entityBuilder.buildVersion(document, user, nextVersionNum, filePath));
        auditLogService.log(user, AuditAction.VERSION_UPLOADED, AuditEntityType.VERSION, version.getId());
        return dtoMapper.toVersionDTO(version);
    }

    @Override
    public List<VersionResponseDTO> getVersionHistory(Integer documentId) {
        entityLookup.findDocumentById(documentId);
        return versionRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(version -> dtoMapper.toVersionDTO(version))
                .toList();
    }

    @Override
    public VersionResponseDTO getVersion(Integer documentId, Integer versionNum) {
        return dtoMapper.toVersionDTO(entityLookup.findVersionByDocumentAndNum(documentId, versionNum));
    }

    @Override
    public VersionResponseDTO getActiveVersion(Integer documentId) {
        return versionRepository.findByDocumentIdAndIsActiveTrue(documentId)
                .map(version -> dtoMapper.toVersionDTO(version))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active (approved) version found for document id: " + documentId));
    }

    @Override
    public Resource downloadFile(Integer documentId, Integer versionNum) {
        Version version = entityLookup.findVersionByDocumentAndNum(documentId, versionNum);
        return fileStorageService.loadFileAsResource(version.getFilePath());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public VersionResponseDTO approveVersion(Integer documentId, Integer versionNum, String comment) {
        Version version = entityLookup.findVersionByDocumentAndNum(documentId, versionNum);

        if (version.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new IllegalStatusException(
                    "Cannot approve version with status: " + version.getStatus() +
                    ". Only UNDER_REVIEW versions can be approved.");
        }

        User reviewer = entityLookup.getCurrentUser();

        versionRepository.findByDocumentIdAndIsActiveTrue(documentId).ifPresent(activeVersion -> {
            activeVersion.setActive(false);
            activeVersion.setStatus(DocumentStatus.ARCHIVED);
            versionRepository.save(activeVersion);
        });

        version.setStatus(DocumentStatus.APPROVED);
        version.setActive(true);
        version.setReviewedBy(reviewer);
        version.setReviewComment(comment);

        Version saved = versionRepository.save(version);
        auditLogService.log(reviewer, AuditAction.VERSION_APPROVED, AuditEntityType.VERSION, saved.getId());
        return dtoMapper.toVersionDTO(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public VersionResponseDTO rejectVersion(Integer documentId, Integer versionNum, String comment) {
        Version version = entityLookup.findVersionByDocumentAndNum(documentId, versionNum);

        if (version.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new IllegalStatusException(
                    "Cannot reject version with status: " + version.getStatus() +
                    ". Only UNDER_REVIEW versions can be rejected.");
        }

        User reviewer = entityLookup.getCurrentUser();

        version.setStatus(DocumentStatus.REJECTED);
        version.setReviewedBy(reviewer);
        version.setReviewComment(comment);

        Version saved = versionRepository.save(version);
        auditLogService.log(reviewer, AuditAction.VERSION_REJECTED, AuditEntityType.VERSION, saved.getId());
        return dtoMapper.toVersionDTO(saved);
    }

    @Override
    public DocumentViewDTO getActiveDocumentView(Integer documentId) {
        Version version = versionRepository.findByDocumentIdAndIsActiveTrue(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active (approved) version found for document id: " + documentId));

        try {
            Resource resource = fileStorageService.loadFileAsResource(version.getFilePath());
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            return dtoMapper.toDocumentViewDTO(version, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read document content.", e);
        }
    }
}
