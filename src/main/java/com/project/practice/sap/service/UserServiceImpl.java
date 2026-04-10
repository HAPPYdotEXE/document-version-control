package com.project.practice.sap.service;

import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.exception.DuplicateResourceException;
import com.project.practice.sap.model.Document;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.RoleRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.service.util.DtoMapper;
import com.project.practice.sap.service.util.EntityLookup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;
    private final EntityLookup entityLookup;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           DocumentRepository documentRepository,
                           PasswordEncoder passwordEncoder,
                           DtoMapper dtoMapper,
                           EntityLookup entityLookup) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.documentRepository = documentRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
        this.entityLookup = entityLookup;
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + user.getEmail());
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleRepository.findByRoleType(RoleType.READER).stream().toList());

        return dtoMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO getUserById(Integer id) {
        return dtoMapper.toUserDTO(entityLookup.findUserById(id));
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> dtoMapper.toUserDTO(user))
                .toList();
    }

    @Override
    public UserResponseDTO getCurrentUser() {
        return dtoMapper.toUserDTO(entityLookup.getCurrentUser());
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Integer id, String password) {
        User user = entityLookup.getCurrentUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        return dtoMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {
        User user = entityLookup.getCurrentUser();
        deleteUser(user.getId());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void deleteUser(Integer id) {
        User user = entityLookup.findUserById(id);
        // documents created by a user who is deleted remain in the server/DB
        for (Document document : user.getDocuments()) {
            document.setCreatedBy(null);
        }

        userRepository.delete(user);
    }
}
