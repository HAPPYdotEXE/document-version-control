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
    UserResponseDTO createdBy,
    UserResponseDTO reviewedBy,   // nullable
    String reviewComment,      // nullable
    Integer documentId
){}
