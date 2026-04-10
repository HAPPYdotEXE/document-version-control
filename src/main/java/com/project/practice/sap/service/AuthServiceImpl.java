package com.project.practice.sap.service;

import com.project.practice.sap.dto.AuthResponse;
import com.project.practice.sap.dto.LoginRequest;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.model.User;
import com.project.practice.sap.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token);
    }

    @Override
    public UserResponseDTO register(User user) {
        return userService.createUser(user);
    }
}
