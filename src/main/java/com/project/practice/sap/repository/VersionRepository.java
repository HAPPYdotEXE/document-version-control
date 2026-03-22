// package com.project.practice.sap.repository;

// import org.springframework.data.jpa.repository.JpaRepository;

// public interface VersionRepository extends JpaRepository {
// }

package com.project.practice.sap.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.practice.sap.model.Version;
import com.project.practice.sap.model.enums.VersionStatus;

public interface VersionRepository extends JpaRepository<Version, Long> {

    List<Version> findByDocumentIdOrderByVersionNumberAsc(Long documentId);

    Optional<Version> findFirstByDocumentIdOrderByVersionNumberDesc(Long documentId);

    List<Version> findByDocumentIdAndStatusOrderByVersionNumberAsc(Long documentId, VersionStatus status);
}