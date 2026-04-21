package com.project.practice.sap.controller;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.service.DocumentService;
import com.project.practice.sap.service.SecurityService;
import com.project.practice.sap.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final DocumentService documentService;
    private final SecurityService securityService;

    public HomeController(UserService userService,
                          DocumentService documentService,
                          SecurityService securityService) {
        this.userService = userService;
        this.documentService = documentService;
        this.securityService = securityService;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        List<DocumentResponseDTO> documents = documentService.getAllDocuments();
        model.addAttribute("documents", documents);
        addUserInfo(authentication, model);
        return "index";
    }

    @GetMapping("/about")
    public String about(Authentication authentication, Model model) {
        addUserInfo(authentication, model);
        return "about";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        UserResponseDTO user = userService.getCurrentUser();
        model.addAttribute("user", user);
        addUserInfo(authentication, model);
        return "profile";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-documents")
    public String myDocuments(Authentication authentication, Model model) {
        List<DocumentResponseDTO> myDocuments = documentService.getCurrentUserDocuments();
        model.addAttribute("documents", myDocuments);
        addUserInfo(authentication, model);
        return "my-documents";
    }

    private void addUserInfo(Authentication authentication, Model model) {
        boolean isLoggedIn = securityService.isLoggedIn(authentication);
        model.addAttribute("isLoggedIn", isLoggedIn);
        if (!isLoggedIn) {
            model.addAttribute("currentRole", "GUEST");
            model.addAttribute("canCreateDocuments", false);
            model.addAttribute("canViewVersions", false);
            model.addAttribute("canReviewVersions", false);
            model.addAttribute("canManageUsers", false);
            return;
        }

        UserResponseDTO user = userService.getCurrentUser();
        model.addAttribute("currentUsername", user.username());
        model.addAttribute("currentEmail", user.email());

        RoleType currentRole = (user.roles() != null && !user.roles().isEmpty())
                ? user.roles().get(0)
                : null;
        String currentRoleName = currentRole != null ? currentRole.name() : "GUEST";
        model.addAttribute("currentRole", currentRoleName);

        boolean canCreateDocuments = currentRole == RoleType.AUTHOR || currentRole == RoleType.ADMIN;
        boolean canViewVersions = currentRole == RoleType.AUTHOR
                || currentRole == RoleType.REVIEWER
                || currentRole == RoleType.ADMIN;
        boolean canReviewVersions = currentRole == RoleType.REVIEWER || currentRole == RoleType.ADMIN;
        boolean canManageUsers = currentRole == RoleType.ADMIN;
        model.addAttribute("canCreateDocuments", canCreateDocuments);
        model.addAttribute("canViewVersions", canViewVersions);
        model.addAttribute("canReviewVersions", canReviewVersions);
        model.addAttribute("canManageUsers", canManageUsers);
    }
}