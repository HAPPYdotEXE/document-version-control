package com.project.practice.sap.service;

import com.project.practice.sap.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserById(Integer id);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getCurrentUser();

    void deleteCurrentUser();

    UserResponseDTO updateUser(String password);

    UserResponseDTO changeUserRole(Integer userId, String role);

    void deleteUser(Integer id);
}
