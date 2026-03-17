package com.project.practice.sap.repository;

import com.project.practice.sap.model.Version;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionRepository extends JpaRepository<Version, Integer> {
}
