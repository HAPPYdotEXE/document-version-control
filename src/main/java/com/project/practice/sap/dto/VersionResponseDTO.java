package com.project.practice.sap.dto;

import com.project.practice.sap.model.enums.DocumentStatus;

import java.time.LocalDateTime;

public record VersionResponseDTO(
        Integer id,
        String versionNum,
        DocumentStatus status,
        boolean isActive,
        String filePath,
        LocalDateTime createdAt,
        UserSummaryDTO createdBy,
        UserSummaryDTO reviewedBy,    // null until a reviewer acts
        String reviewComment,          // null until a reviewer acts
        Integer documentId
) {}
