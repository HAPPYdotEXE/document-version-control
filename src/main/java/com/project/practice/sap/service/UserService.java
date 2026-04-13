package com.project.practice.sap.service;

import com.project.practice.sap.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserById(Integer id);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getCurrentUser();

    void deleteCurrentUser();

    UserResponseDTO updateUser(String password);

    void deleteUser(Integer id);
}
