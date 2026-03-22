// package com.project.practice.sap.repository;

// import org.springframework.data.jpa.repository.JpaRepository;

// public interface DocumentRepository extends JpaRepository {
// }


package com.project.practice.sap.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.practice.sap.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}