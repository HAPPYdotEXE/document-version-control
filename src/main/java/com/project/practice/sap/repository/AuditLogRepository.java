package com.project.practice.sap.repository;

import com.project.practice.sap.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    List<AuditLog> findByEntityType(String entityType);

    List<AuditLog> findByPerformedById(Integer userId);
}
