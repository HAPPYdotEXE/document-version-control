package com.project.practice.sap.service.util;

import com.project.practice.sap.repository.AuditLogRepository;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.VersionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserReferenceUtil {

    private final DocumentRepository documentRepository;
    private final VersionRepository versionRepository;
    private final AuditLogRepository auditLogRepository;

    public UserReferenceUtil(DocumentRepository documentRepository,
                             VersionRepository versionRepository,
                             AuditLogRepository auditLogRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // nullifies all FK references to a user across every table before deletion.
    @Transactional
    public void clearAllReferencesForUser(Integer userId) {
        documentRepository.clearCreatedByForUser(userId);
        versionRepository.clearCreatedByForUser(userId);
        versionRepository.clearReviewedByForUser(userId);
        auditLogRepository.clearPerformedByForUser(userId);
    }
}
