package com.project.practice.sap.service;

import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.exception.DuplicateResourceException;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.RoleRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.service.util.DtoMapper;
import com.project.practice.sap.service.util.EntityLookup;
import com.project.practice.sap.service.util.UserReferenceUtil;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final EntityLookup entityLookup;
    private final AuditLogService auditLogService;
    private final UserReferenceUtil userReferenceUtil;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           DtoMapper dtoMapper,
                           EntityLookup entityLookup,
                           AuditLogService auditLogService,
                           UserReferenceUtil userReferenceUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
        this.entityLookup = entityLookup;
        this.auditLogService = auditLogService;
        this.userReferenceUtil = userReferenceUtil;
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

        User saved = userRepository.save(user);
        auditLogService.log(saved, "USER_CREATED", "USER", saved.getId());
        return dtoMapper.toUserDTO(saved);
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
    public UserResponseDTO updateUser(String password) {
        User user = entityLookup.getCurrentUser();
        // password must be set so Hibernate's pre-update entity validation
        // does not fail on the @NotBlank constraint of the @Transient field
        user.setPassword(password);
        user.setPasswordHash(passwordEncoder.encode(password));
        auditLogService.log(user, "USER_UPDATED", "USER", user.getId());
        return dtoMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {
        User user = entityLookup.getCurrentUser();
        Integer userId = user.getId();
        userReferenceUtil.clearAllReferencesForUser(userId);
        userRepository.delete(user);
        auditLogService.log(null, "USER_DELETED", "USER", userId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void deleteUser(Integer id) {
        User user = entityLookup.findUserById(id);
        User actor = entityLookup.getCurrentUser();
        userReferenceUtil.clearAllReferencesForUser(id);
        userRepository.delete(user);
        // using ternary operation in case the admin self deletes with id
        auditLogService.log(actor.getId().equals(id) ? null : actor, "USER_DELETED", "USER", id);
    }
}
