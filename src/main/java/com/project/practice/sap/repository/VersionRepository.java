package com.project.practice.sap.repository;

import com.project.practice.sap.model.Version;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
