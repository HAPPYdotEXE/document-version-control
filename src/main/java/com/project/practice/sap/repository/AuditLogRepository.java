package com.project.practice.sap.repository;

import com.project.practice.sap.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    // retrieves all audit log entries for a specific entity
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Integer entityId);

    // retrieves all actions performed by a specific user
    List<AuditLog> findByCreatedById(Integer userId);
}
