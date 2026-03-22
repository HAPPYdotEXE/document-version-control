package com.project.practice.sap.service;

import com.project.practice.sap.dto.CreateDocumentRequestDTO;
import com.project.practice.sap.dto.DocumentResponseDTO;

public interface DocumentService {
    DocumentResponseDTO createDocument(CreateDocumentRequestDTO request);
    DocumentResponseDTO getDocumentById(Long id);
}
