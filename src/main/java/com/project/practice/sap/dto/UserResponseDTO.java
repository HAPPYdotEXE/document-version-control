package com.project.practice.sap.dto;

import java.time.LocalDateTime;

public record UserResponseDTO (

    Integer id,
    String username,
    String email,
    LocalDateTime createdAt
){}
