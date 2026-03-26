package com.project.practice.sap.service;

import com.project.practice.sap.dto.CreateUserRequest;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.exception.DuplicateResourceException;
import com.project.practice.sap.exception.ResourceNotFoundException;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.RoleRepository;
import com.project.practice.sap.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(roleRepository.findByRoleType(RoleType.READER).stream().toList());

        return dtoMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return dtoMapper.toUserDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> dtoMapper.toUserDTO(user))
                .toList();
    }
}
