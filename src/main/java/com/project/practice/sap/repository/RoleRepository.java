package com.project.practice.sap.repository;

import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleType(RoleType roleType);
}
