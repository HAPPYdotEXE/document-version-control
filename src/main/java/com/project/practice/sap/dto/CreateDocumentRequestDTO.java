package com.project.practice.sap.dto;

public record CreateDocumentRequestDTO(
    String title,
    String description,
    Long authorId,
    String initialContent,
    String changeSummary
) {
}