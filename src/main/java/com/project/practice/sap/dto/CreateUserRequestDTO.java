package com.project.practice.sap.dto;

import java.util.Set;

import com.project.practice.sap.model.enums.RoleType;

public record CreateUserRequestDTO(
    String userName,
    String email,
    String password,
    Set<RoleType> roles
) {
}
