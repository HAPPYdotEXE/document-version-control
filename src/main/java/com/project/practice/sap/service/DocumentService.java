package com.project.practice.sap.service;

import com.project.practice.sap.dto.DocumentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponseDTO createDocument(String name, MultipartFile file);

    DocumentResponseDTO getDocumentById(Integer id);

    List<DocumentResponseDTO> getAllDocuments();

    DocumentResponseDTO updateDocument(Integer id, String name, MultipartFile file);

    void deleteDocument(Integer id);
}
