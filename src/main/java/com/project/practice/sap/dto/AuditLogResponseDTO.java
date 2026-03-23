package com.project.practice.sap.dto;

import java.time.LocalDateTime;

public record AuditLogResponseDTO(
    Integer id,
    String action,
    String details,
    String entityType,
    Integer entityId,
    LocalDateTime timeStamp,
    UserResponseDTO createdBy
){}

