package com.project.practice.sap.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.practice.sap.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
