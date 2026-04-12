package com.project.practice.sap.repository;

import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VersionRepository extends JpaRepository<Version, Integer> {

    // Finds the current active version (deactivate this one and make the new one active)
    Optional<Version> findByDocumentIdAndIsActiveTrue(Integer documentId);

    // returns the full version history for a document in ascending order
    List<Version> findByDocumentIdOrderByCreatedAtAsc(Integer documentId);

    // checks the last version num so we can compute the next one
    int countByDocumentId(Integer documentId);

    // checks if this is the very first document upload
    boolean existsByDocumentId(Integer documentId);

    // enforces the one-pending-review rule: only one UNDER_REVIEW version allowed per document at a time
    boolean existsByDocumentIdAndStatus(Integer documentId, DocumentStatus status);

    // finds a specific version by its document version number (not the global DB versionId)
    Optional<Version> findByDocumentIdAndVersionNum(Integer documentId, Integer versionNum);

    // set createdBy/reviewedBy to null upon ser deletion
    @Modifying
    @Query("UPDATE Version v SET v.createdBy = null WHERE v.createdBy.id = :userId")
    void clearCreatedByForUser(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE Version v SET v.reviewedBy = null WHERE v.reviewedBy.id = :userId")
    void clearReviewedByForUser(@Param("userId") Integer userId);
}
