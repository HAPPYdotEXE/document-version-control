package com.project.practice.sap.service;

import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.enums.RoleType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Override
    public boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @Override
    public boolean hasRole(Authentication authentication, RoleType roleType) {
        if (!isLoggedIn(authentication)) {
            return false;
        }

        String fullRole = "ROLE_" + roleType.name();

        return authentication.getAuthorities().stream()
                .anyMatch(a -> fullRole.equals(a.getAuthority()));
    }

    @Override
    public boolean canReview(Authentication authentication) {
        return hasRole(authentication, RoleType.REVIEWER) || hasRole(authentication, RoleType.ADMIN);
    }

    @Override
    public boolean canUploadVersion(Authentication authentication) {
        return hasRole(authentication, RoleType.AUTHOR) || hasRole(authentication, RoleType.ADMIN);
    }
}