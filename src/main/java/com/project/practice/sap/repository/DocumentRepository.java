package com.project.practice.sap.repository;

import com.project.practice.sap.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    // checks if name doesn't already exist upon creating a document
    Optional<Document> findByName(String name);

    // duplicate check without loading the full entity
    boolean existsByName(String name);

    List<Document> findByCreatedById(Integer createdById);

    // set CreatedBy to null if user is deleted
    @Modifying
    @Query("UPDATE Document d SET d.createdBy = null WHERE d.createdBy.id = :userId")
    void clearCreatedByForUser(@Param("userId") Integer userId);
}
