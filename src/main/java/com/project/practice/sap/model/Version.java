package com.project.practice.sap.model;

import com.project.practice.sap.model.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="versions")
@EntityListeners(AuditingEntityListener.class)
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="version_num")
    private Integer versionNum;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private DocumentStatus status;

    @Column(name="is_active", nullable = false)
    private boolean isActive;

    @Column(name="file_path", nullable = false)
    private String filePath;

    @CreatedDate
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // infinite loop issue with lombok
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = true)
    @ToString.Exclude // infinite loop issue with lombok
    private User reviewedBy;

    @Column(name="review_comment")
    private String reviewComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    @ToString.Exclude // infinite loop issue with lombok
    private Document document;
}
