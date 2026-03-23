package com.project.practice.sap.dto;

import java.time.LocalDateTime;

public record DocumentResponseDTO(

    Integer id,
    String name,
    LocalDateTime createdAt,
    UserResponseDTO createdBy
){}

