//package com.project.practice.sap.service;
//
//import com.project.practice.sap.dto.AuthResponse;
//import com.project.practice.sap.dto.LoginRequest;
//import com.project.practice.sap.dto.UserResponseDTO;
//import com.project.practice.sap.model.User;
//
//public interface AuthService {
//    AuthResponse login(LoginRequest request);
//    UserResponseDTO register(User user);
//}


package com.project.practice.sap.service;

import com.project.practice.sap.dto.AuthResponse;
import com.project.practice.sap.dto.LoginRequest;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.model.User;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponseDTO register(User user, String role);
}