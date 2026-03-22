package com.project.practice.sap.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.practice.sap.dto.CreateDocumentRequestDTO;
import com.project.practice.sap.dto.CreateVersionRequestDTO;
import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.service.DocumentService;
import com.project.practice.sap.service.VersionService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final VersionService versionService;

    public DocumentController(DocumentService documentService, VersionService versionService) {
        this.documentService = documentService;
        this.versionService = versionService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponseDTO> createDocument(@RequestBody CreateDocumentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createDocument(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<VersionResponseDTO>> getVersionsByDocumentId(@PathVariable Long id) {
        return ResponseEntity.ok(versionService.getVersionsByDocumentId(id));
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<VersionResponseDTO> createVersion(
            @PathVariable Long id,
            @RequestBody CreateVersionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(versionService.createVersion(id, request));
    }
}
