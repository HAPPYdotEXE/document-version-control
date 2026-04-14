package com.project.practice.sap.controller;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Documents", description = "Document management")
@Validated
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Create a document and upload its first version as a .txt file — AUTHOR or ADMIN only")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponseDTO> createDocument(
            @NotBlank(message = "Document name must not be blank")
            @RequestParam String name,
            @RequestParam MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createDocument(name, file));
    }

    @Operation(summary = "Get all documents")
    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @Operation(summary = "Get a document by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getDocumentById(@PathVariable Integer id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @Operation(summary = "Rename a document — AUTHOR or ADMIN only")
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> updateDocument(
            @PathVariable Integer id,
            @NotBlank(message = "Document name must not be blank") @RequestParam String name) {
        return ResponseEntity.ok(documentService.updateDocument(id, name));
    }

    @Operation(summary = "Delete a document and all its versions — AUTHOR or ADMIN only")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
