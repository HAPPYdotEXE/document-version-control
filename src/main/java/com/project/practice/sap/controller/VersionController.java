package com.project.practice.sap.controller;

import com.project.practice.sap.dto.ApproveVersionRequest;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.service.VersionService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents/{documentId}/versions")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<VersionResponseDTO> uploadNewVersion(
            @PathVariable Integer documentId,
            @RequestParam Integer userId,
            @RequestParam MultipartFile file) {

        VersionResponseDTO created = versionService.uploadNewVersion(documentId, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{versionId}")
    public ResponseEntity<VersionResponseDTO> getVersion(
            @PathVariable Integer documentId,
            @PathVariable Integer versionId){
        return ResponseEntity.ok(versionService.getVersion(documentId, versionId));
    }

    @GetMapping
    public ResponseEntity<List<VersionResponseDTO>> getVersionHistory(@PathVariable Integer documentId) {
        return ResponseEntity.ok(versionService.getVersionHistory(documentId));
    }

    // returns 404 if no approved versions yet
    @GetMapping("/active")
    public ResponseEntity<VersionResponseDTO> getActiveVersion(@PathVariable Integer documentId) {
        return ResponseEntity.ok(versionService.getActiveVersion(documentId));
    }

    @GetMapping("/{versionId}/file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Integer documentId,
            @PathVariable Integer versionId) {

        Resource file = versionService.downloadFile(documentId, versionId);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PutMapping("/{versionId}/approve")
    public ResponseEntity<VersionResponseDTO> approveVersion(
            @PathVariable Integer documentId,
            @PathVariable Integer versionId,
            @RequestBody ApproveVersionRequest request) {

        return ResponseEntity.ok(versionService.approveVersion(documentId, versionId, request));
    }

    @PutMapping("/{versionId}/reject")
    public ResponseEntity<VersionResponseDTO> rejectVersion(
            @PathVariable Integer documentId,
            @PathVariable Integer versionId,
            @RequestBody ApproveVersionRequest request) {

        return ResponseEntity.ok(versionService.rejectVersion(documentId, versionId, request));
    }
}
