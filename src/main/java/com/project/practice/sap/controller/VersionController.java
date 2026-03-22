package com.project.practice.sap.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.practice.sap.dto.ReviewVersionRequestDTO;
import com.project.practice.sap.dto.VersionResponseDTO;
import com.project.practice.sap.service.VersionService;

@RestController
@RequestMapping("/versions")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VersionResponseDTO> getVersionById(@PathVariable Long id) {
        return ResponseEntity.ok(versionService.getVersionById(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<VersionResponseDTO> approveVersion(
            @PathVariable Long id,
            @RequestBody ReviewVersionRequestDTO request) {
        return ResponseEntity.ok(versionService.approveVersion(id, request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<VersionResponseDTO> rejectVersion(
            @PathVariable Long id,
            @RequestBody ReviewVersionRequestDTO request) {
        return ResponseEntity.ok(versionService.rejectVersion(id, request));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<VersionResponseDTO> activateVersion(@PathVariable Long id) {
        return ResponseEntity.ok(versionService.activateVersion(id));
    }
}