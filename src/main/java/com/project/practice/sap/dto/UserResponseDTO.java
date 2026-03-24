package com.project.practice.sap.dto;

import com.project.practice.sap.model.Role;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponseDTO (

    Integer id,
    String username,
    String email,
    List<Role> roles,
    LocalDateTime createdAt
){}
