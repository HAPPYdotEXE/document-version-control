package com.project.practice.sap.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

    @NotBlank(message = "Username must not be blank")
    String username,

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password    // plain text — the service layer will hash this before saving
) {
    public CreateUserRequest {
//        username = username != null ? username.trim() : null;
        username = username.trim();
//        email    = email    != null ? email.trim()    : null;
        email    = email.trim();

    }
}
