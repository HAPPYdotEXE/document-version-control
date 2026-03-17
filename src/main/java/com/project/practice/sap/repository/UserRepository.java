package com.project.practice.sap.repository;

import com.project.practice.sap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
