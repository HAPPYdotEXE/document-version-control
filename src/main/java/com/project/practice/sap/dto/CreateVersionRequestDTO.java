package com.project.practice.sap.dto;

public record CreateVersionRequestDTO(
    Long createdById,
    String content,
    String changeSummary
) {
}
