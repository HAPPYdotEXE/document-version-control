package com.project.practice.sap.dto;

import java.time.LocalDateTime;

import com.project.practice.sap.model.enums.VersionStatus;

public record VersionResponseDTO(
    Long id,
    Long documentId,
    Integer versionNumber,
    String content,
    String changeSummary,
    VersionStatus status,
    String createdByUsername,
    LocalDateTime createdAt,
    String reviewedByUsername,
    LocalDateTime reviewedAt,
    String reviewComment
) {
}