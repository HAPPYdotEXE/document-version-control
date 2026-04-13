package com.project.practice.sap.controller;

import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Versions", description = "Version upload and review workflow")
@Validated
@RestController
@RequestMapping("/api/v1/documents/{documentId}/versions")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @Operation(summary = "Upload a new .txt version — AUTHOR or ADMIN only. Only one version can be UNDER_REVIEW at a time.")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<VersionResponseDTO> uploadNewVersion(
            @PathVariable Integer documentId,
            @RequestParam MultipartFile file) {

        return ResponseEntity.status(HttpStatus.CREATED).body(versionService.uploadNewVersion(documentId, file));
    }

    @Operation(summary = "Get a specific version by its version number")
    @GetMapping("/{versionNum}")
    public ResponseEntity<VersionResponseDTO> getVersion(
            @PathVariable Integer documentId,
            @PathVariable Integer versionNum) {
        return ResponseEntity.ok(versionService.getVersion(documentId, versionNum));
    }

    @Operation(summary = "Get the full version history of a document")
    @GetMapping
    public ResponseEntity<List<VersionResponseDTO>> getVersionHistory(@PathVariable Integer documentId) {
        return ResponseEntity.ok(versionService.getVersionHistory(documentId));
    }

    // returns 404 if no version has been approved yet
    @Operation(summary = "Get the currently active (approved) version")
    @GetMapping("/active")
    public ResponseEntity<VersionResponseDTO> getActiveVersion(@PathVariable Integer documentId) {
        return ResponseEntity.ok(versionService.getActiveVersion(documentId));
    }

    @Operation(summary = "Download the .txt file attached to a version")
    @GetMapping("/{versionNum}/file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Integer documentId,
            @PathVariable Integer versionNum) {

        Resource file = versionService.downloadFile(documentId, versionNum);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @Operation(summary = "Approve an UNDER_REVIEW version — REVIEWER or ADMIN only. Sets it as active and archives the previous active version.")
    @PutMapping("/{versionNum}/approve")
    public ResponseEntity<VersionResponseDTO> approveVersion(
            @PathVariable Integer documentId,
            @PathVariable Integer versionNum,
            @RequestParam(required = false) String comment) {

        return ResponseEntity.ok(versionService.approveVersion(documentId, versionNum, comment));
    }

    @Operation(summary = "Reject an UNDER_REVIEW version with an optional comment — REVIEWER or ADMIN only")
    @PutMapping("/{versionNum}/reject")
    public ResponseEntity<VersionResponseDTO> rejectVersion(
            @PathVariable Integer documentId,
            @PathVariable Integer versionNum,
            @RequestParam(required = false) String comment) {

        return ResponseEntity.ok(versionService.rejectVersion(documentId, versionNum, comment));
    }
}
