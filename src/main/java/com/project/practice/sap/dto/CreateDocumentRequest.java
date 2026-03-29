package com.project.practice.sap.dto;

public record CreateDocumentRequest(
        String name,
        Integer userId
) {
    public CreateDocumentRequest {
        name = name.trim();
    }
}

