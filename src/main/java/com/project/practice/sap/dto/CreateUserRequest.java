package com.project.practice.sap.dto;

public record CreateUserRequest (

    String username,
    String email,
    String password    // plain text — the service layer will hash this before saving
){}
