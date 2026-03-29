package com.project.practice.sap.service;

import com.project.practice.sap.dto.CreateUserRequest;
import com.project.practice.sap.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(CreateUserRequest request);

    UserResponseDTO getUserById(Integer id);

    List<UserResponseDTO> getAllUsers();
}
