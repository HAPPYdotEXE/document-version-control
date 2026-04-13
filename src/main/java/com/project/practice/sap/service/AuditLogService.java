package com.project.practice.sap.service;

import com.project.practice.sap.dto.AuditLogResponseDTO;
import com.project.practice.sap.model.User;

import java.util.List;

public interface AuditLogService {

    void log(User actor, String action, String entityType, Integer entityId);

    List<AuditLogResponseDTO> getAllLogs();

    List<AuditLogResponseDTO> getLogsByEntityType(String entityType);

    List<AuditLogResponseDTO> getLogsByUser(Integer userId);
}
