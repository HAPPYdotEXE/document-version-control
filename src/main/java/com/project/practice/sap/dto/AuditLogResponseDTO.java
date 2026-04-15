package com.project.practice.sap.dto;

import java.time.LocalDateTime;

public record AuditLogResponseDTO(
        Integer id,
        UserSummaryDTO performedBy,
        String action,
        String entityType,
        Integer entityId,
        LocalDateTime timeStamp
){}
