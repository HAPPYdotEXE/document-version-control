package com.project.practice.sap.service;

import com.project.practice.sap.dto.AuditLogResponseDTO;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.AuditAction;
import com.project.practice.sap.model.enums.AuditEntityType;

import java.util.List;

public interface AuditLogService {

    void log(User actor, AuditAction action, AuditEntityType entityType, Integer entityId);

    List<AuditLogResponseDTO> getAllLogs();

    List<AuditLogResponseDTO> getLogsByEntityType(AuditEntityType entityType);

    List<AuditLogResponseDTO> getLogsByUser(Integer userId);
}
