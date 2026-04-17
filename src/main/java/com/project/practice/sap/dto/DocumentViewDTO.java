package com.project.practice.sap.dto;

import java.time.LocalDateTime;

public record DocumentViewDTO(
        Integer documentId,
        String documentName,
        Integer versionNumber,
        String content,
        LocalDateTime createdAt
) {}