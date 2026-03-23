package com.project.practice.sap.repository;

import com.project.practice.sap.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    // checks if name doesn't already exist upon creating a document
    Optional<Document> findByName(String name);

    // duplicate check without loading the full entity
    boolean existsByName(String name);
}
