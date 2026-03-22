// package com.project.practice.sap.service;

// import org.springframework.stereotype.Service;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import com.project.practice.sap.repository.UserRepository;
// import com.project.practice.sap.model.User;
// import com.project.practice.sap.dto.UserResponseDTO;
// import com.project.practice.sap.dto.CreateUserRequestDTO;

// @Service
// public class UserService {
//     private final UserRepository repository;
//     private final PasswordEncoder passwordEncoder;

//     public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
//         this.repository = repository;
//         this.passwordEncoder = passwordEncoder;
//     }

//     public UserResponseDTO createUser(CreateUserRequestDTO request) {
//         User user = new User();
//         user.setUsername(request.userName());
//         user.setEmail(request.email());
//         user.setPasswordHash(passwordEncoder.encode(request.password()));
//         User saved = repository.save(user);
//         return toResponse(saved);
//     }

//     public UserResponseDTO getUserById(Long id) {
//         User user = repository.findById(id)
//             .orElseThrow(() -> new RuntimeException("User not found"));
//         return toResponse(user);
//     }

//     private UserResponseDTO toResponse(User user) {
//         return new UserResponseDTO(user.getUsername(), user.getEmail());
//     }
// }



// package com.project.practice.sap.service;

// import org.springframework.stereotype.Service;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import com.project.practice.sap.repository.UserRepository;
// import com.project.practice.sap.model.User;
// import com.project.practice.sap.dto.UserResponseDTO;
// import com.project.practice.sap.dto.CreateUserRequestDTO;

// @Service
// public class UserService {
//     private final UserRepository repository;
//     private final PasswordEncoder passwordEncoder;

//     public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
//         this.repository = repository;
//         this.passwordEncoder = passwordEncoder;
//     }

//     public UserResponseDTO createUser(CreateUserRequestDTO request) {
//         User user = new User();
//         user.setUsername(request.userName());
//         user.setEmail(request.email());
//         user.setPasswordHash(passwordEncoder.encode(request.password()));

//         User saved = repository.save(user);
//         return toResponse(saved);
//     }

//     public UserResponseDTO getUserById(Long id) {
//         User user = repository.findById(id)
//             .orElseThrow(() -> new RuntimeException("User not found"));
//         return toResponse(user);
//     }

//     private UserResponseDTO toResponse(User user) {
//         return new UserResponseDTO(user.getUsername(), user.getEmail());
//     }
// }



package com.project.practice.sap.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.practice.sap.dto.CreateUserRequestDTO;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.exception.BadRequestException;
import com.project.practice.sap.exception.NotFoundException;
import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.RoleRepository;
import com.project.practice.sap.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        if (repository.existsByUsername(request.userName())) {
            throw new BadRequestException("Username already exists");
        }

        if (repository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.userName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        Set<RoleType> requestedRoles = (request.roles() == null || request.roles().isEmpty())
                ? Set.of(RoleType.AUTHOR)
                : request.roles();

        Set<Role> roles = requestedRoles.stream()
                .map(roleType -> roleRepository.findByRoleType(roleType)
                        .orElseThrow(() -> new BadRequestException("Role not found: " + roleType)))
                .collect(Collectors.toSet());

        user.setRoles(roles);

        User saved = repository.save(user);
        return toResponse(saved);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(user);
    }

    private UserResponseDTO toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleType().name())
                .collect(Collectors.toSet());

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}