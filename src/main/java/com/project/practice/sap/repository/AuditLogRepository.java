package com.project.practice.sap.repository;

import com.project.practice.sap.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    List<AuditLog> findByEntityType(String entityType);

    List<AuditLog> findByPerformedById(Integer userId);


    // custom query to set userId to null if user is deleted
    // flushAutomatically = true so we can log and clear without messing the order and getting errors for clearing before log completes
    @Modifying(flushAutomatically = true)
    @Query("UPDATE AuditLog a SET a.performedBy = null WHERE a.performedBy.id = :userId")
    void clearPerformedByForUser(@Param("userId") Integer userId);
}
