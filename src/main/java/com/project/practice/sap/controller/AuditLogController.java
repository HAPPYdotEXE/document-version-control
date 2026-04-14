package com.project.practice.sap.controller;

import com.project.practice.sap.dto.AuditLogResponseDTO;
import com.project.practice.sap.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Audit Logs", description = "System audit trail — ADMIN only")
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "Get the full system audit trail")
    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @Operation(summary = "Get audit logs filtered by entity type — valid values: USER, DOCUMENT, VERSION")
    @GetMapping("/{entityType}")
    public ResponseEntity<List<AuditLogResponseDTO>> getLogsByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditLogService.getLogsByEntityType(entityType));
    }

    @Operation(summary = "Get all audit logs where the specified user was the actor")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponseDTO>> getLogsByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(auditLogService.getLogsByUser(userId));
    }
}
