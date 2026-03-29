package com.project.practice.sap.controller;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.service.DocumentService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponseDTO> createDocument(
            @NotBlank(message = "Document name must not be blank") // applies to the request below
            @RequestParam String name,
            @RequestParam Integer userId,
            @RequestParam MultipartFile file) {

        DocumentResponseDTO created = documentService.createDocument(name, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getDocumentById(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }
}
