package com.project.practice.sap.service;

import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.User;
import com.project.practice.sap.model.enums.AuditAction;
import com.project.practice.sap.model.enums.AuditEntityType;
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
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;
    private final EntityLookup entityLookup;
    private final AuditLogService auditLogService;
    private final UserReferenceUtil userReferenceUtil;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           DtoMapper dtoMapper,
                           EntityLookup entityLookup,
                           AuditLogService auditLogService,
                           UserReferenceUtil userReferenceUtil,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
        this.entityLookup = entityLookup;
        this.auditLogService = auditLogService;
        this.userReferenceUtil = userReferenceUtil;
        this.roleRepository = roleRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO getUserById(Integer id) {
        return dtoMapper.toUserDTO(entityLookup.findUserById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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
        auditLogService.log(user, AuditAction.USER_UPDATED, AuditEntityType.USER, user.getId());
        return dtoMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO changeUserRole(Integer id, String role) {
        User user = entityLookup.findUserById(id);

        RoleType roleType;
        try {
            roleType = RoleType.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        Role newRole = roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleType));

        if (user.getRoles() == null) {
            user.setRoles(new java.util.ArrayList<>());
        } else {
            user.getRoles().clear();
        }
        user.getRoles().add(newRole);

        // workaround за @Transient password
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword("temporary-password-for-validation");
        }

        User savedUser = userRepository.saveAndFlush(user);

        auditLogService.log(
                entityLookup.getCurrentUser(),
                AuditAction.USER_UPDATED,
                AuditEntityType.USER,
                savedUser.getId()
        );

        return dtoMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {
        User user = entityLookup.getCurrentUser();
        Integer userId = user.getId();
        userReferenceUtil.clearAllReferencesForUser(userId);
        userRepository.delete(user);
        auditLogService.log(null, AuditAction.USER_DELETED, AuditEntityType.USER, userId);
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
        auditLogService.log(actor.getId().equals(id) ? null : actor, AuditAction.USER_DELETED, AuditEntityType.USER, id);
    }
}
