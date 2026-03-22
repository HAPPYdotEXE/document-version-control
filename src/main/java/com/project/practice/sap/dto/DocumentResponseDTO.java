package com.project.practice.sap.dto;

import java.time.LocalDateTime;

public record DocumentResponseDTO(
    Long id,
    String title,
    String description,
    String authorUsername,
    Long activeVersionId,
    Integer activeVersionNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
