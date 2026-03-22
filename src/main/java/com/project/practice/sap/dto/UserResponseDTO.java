package com.project.practice.sap.dto;

import java.util.Set;

public record UserResponseDTO(
    Long id,
    String userName,
    String email,
    Set<String> roles
) {
}