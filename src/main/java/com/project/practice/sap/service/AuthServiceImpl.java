package com.project.practice.sap.service;

import com.project.practice.sap.dto.AuthResponse;
import com.project.practice.sap.dto.LoginRequest;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.exception.DuplicateResourceException;
import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.AuditAction;
import com.project.practice.sap.model.enums.AuditEntityType;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.RoleRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.security.JwtUtil;
import com.project.practice.sap.service.util.DtoMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           DtoMapper dtoMapper,
                           AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
        this.auditLogService = auditLogService;
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
    @Transactional
    public UserResponseDTO register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + user.getEmail());
        }

        Role readerRole = roleRepository.findByRoleType(RoleType.READER)
                .orElseThrow(() -> new IllegalStateException("Role not found: READER"));

        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of(readerRole));

        User saved = userRepository.save(user);
        auditLogService.log(saved, AuditAction.USER_CREATED, AuditEntityType.USER, saved.getId());

        return dtoMapper.toUserDTO(saved);
    }
}
