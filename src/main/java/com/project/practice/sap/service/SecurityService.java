package com.project.practice.sap.service;

import com.project.practice.sap.model.enums.RoleType;
import org.springframework.security.core.Authentication;

public interface SecurityService {
    boolean isLoggedIn(Authentication authentication);
    boolean hasRole(Authentication authentication, RoleType role);
    boolean canReview(Authentication authentication);
    boolean canUploadVersion(Authentication authentication);
}