package com.project.practice.sap.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.enums.RoleType;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleType(RoleType roleType);
}
