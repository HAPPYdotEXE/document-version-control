package com.project.practice.sap.controller;

import com.project.practice.sap.dto.AuthResponse;
import com.project.practice.sap.dto.LoginRequest;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.model.User;
import com.project.practice.sap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Register a new account or log in to receive a JWT token")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Log in and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Register a new account — assigned READER role by default")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid User user, String role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(user, role));
    }
}
