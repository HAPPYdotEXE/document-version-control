package com.project.practice.sap.dto;

import com.project.practice.sap.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponseDTO (

        Integer id,
        String username,
        String email,
        List<RoleType> roles,
        LocalDateTime createdAt
){}
