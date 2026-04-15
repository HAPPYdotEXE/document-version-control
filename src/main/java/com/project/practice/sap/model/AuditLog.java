package com.project.practice.sap.model;

import com.project.practice.sap.model.enums.AuditAction;
import com.project.practice.sap.model.enums.AuditEntityType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name="audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // infinite loop issue with lombok
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(name="action")
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name="entity_type", nullable = false)
    private AuditEntityType entityType;

    @Column(name="entity_id", nullable = true)
    private Integer entityId;

    @CreatedDate
    @Column(name="timestamp", nullable = false, updatable = false)
    private LocalDateTime timeStamp;
}
