// package com.project.practice.sap.model;

// public class Version {
// }

package com.project.practice.sap.model;

import java.time.LocalDateTime;

import com.project.practice.sap.model.enums.VersionStatus;

import jakarta.persistence.*;

@Entity
@Table(
    name = "document_versions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"document_id", "version_number"})
    }
)
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 500)
    private String changeSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 1000)
    private String reviewComment;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = VersionStatus.DRAFT;
        }
    }

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public String getContent() {
        return content;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public void setStatus(VersionStatus status) {
        this.status = status;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
