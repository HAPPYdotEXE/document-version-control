// package com.project.practice.sap.repository;

// import org.springframework.data.jpa.repository.JpaRepository;

// public interface AuditLogRepository extends JpaRepository {
// }

package com.project.practice.sap.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.practice.sap.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}