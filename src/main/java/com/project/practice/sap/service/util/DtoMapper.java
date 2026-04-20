package com.project.practice.sap.service.util;

import com.project.practice.sap.dto.AuditLogResponseDTO;
import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.dto.UserSummaryDTO;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.model.AuditLog;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.VersionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtoMapper {
    private final VersionRepository versionRepository;

    public DtoMapper(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }
    public UserResponseDTO toUserDTO(User user) {
        List<RoleType> roleTypes = user.getRoles()
                .stream()
                .map(role -> role.getRoleType())
                .toList();

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleTypes,
                user.getCreatedAt()
        );
    }

    public UserSummaryDTO toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummaryDTO(user.getId(), user.getUsername());
    }

    public DocumentResponseDTO toDocumentDTO(Document document) {
        return new DocumentResponseDTO(
                document.getId(),
                document.getName(),
                document.getCreatedAt(),
                toUserSummary(document.getCreatedBy()),
                versionRepository.countByDocumentId(document.getId())
        );
    }

    public AuditLogResponseDTO toAuditLogDTO(AuditLog log) {
        return new AuditLogResponseDTO(
                log.getId(),
                toUserSummary(log.getPerformedBy()),
                log.getAction().name(),
                log.getEntityType().name(),
                log.getEntityId(),
                log.getTimeStamp()
        );
    }

    public VersionResponseDTO toVersionDTO(Version version) {
        return new VersionResponseDTO(
                version.getId(),
                version.getVersionNum(),
                version.getStatus(),
                version.isActive(),
                version.getFilePath(),
                version.getCreatedAt(),
                toUserSummary(version.getCreatedBy()),
                toUserSummary(version.getReviewedBy()),
                version.getReviewComment(),
                version.getDocument().getId()
        );
    }
}
