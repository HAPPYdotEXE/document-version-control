package com.project.practice.sap.repository;

import com.project.practice.sap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // checks if username exists on login
    Optional<User> findByUsername(String username);

    // will be used during registration to prevent duplicate emails
    boolean existsByEmail(String email);

    // will be used during registration to prevent duplicate usernames
    boolean existsByUsername(String username);
}
