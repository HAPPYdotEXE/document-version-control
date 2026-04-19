package com.project.practice.sap.controller;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.model.User;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.service.DocumentService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final DocumentService documentService;

    public HomeController(UserRepository userRepository, DocumentService documentService) {
        this.userRepository = userRepository;
        this.documentService = documentService;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        boolean isLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isLoggedIn);

        boolean canCreateDocuments = false;
        boolean canEditDocuments = false;
        boolean canDeleteDocuments = false;
        boolean canViewVersions = false;
        boolean canReviewVersions = false;
        boolean canManageUsers = false;
        String currentRole = "GUEST";

        if (isLoggedIn) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);

            if (user != null) {
                model.addAttribute("currentUsername", user.getUsername());
                model.addAttribute("currentEmail", user.getEmail());

                currentRole =
                        user.getRoles() != null && !user.getRoles().isEmpty()
                                ? user.getRoles().get(0).getRoleType().name()
                                : "User";

                model.addAttribute("currentRole", currentRole);
                canCreateDocuments = "AUTHOR".equals(currentRole) || "ADMIN".equals(currentRole);
                canEditDocuments = "AUTHOR".equals(currentRole) || "ADMIN".equals(currentRole);
                canDeleteDocuments = "ADMIN".equals(currentRole);
                canViewVersions = "AUTHOR".equals(currentRole) || "REVIEWER".equals(currentRole) || "ADMIN".equals(currentRole);
                canReviewVersions = "REVIEWER".equals(currentRole) || "ADMIN".equals(currentRole);
                canManageUsers = "ADMIN".equals(currentRole);
            }
        }

        try {
            List<DocumentResponseDTO> documents = documentService.getAllDocuments();
            model.addAttribute("documents", documents);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("documents", java.util.Collections.emptyList());
            model.addAttribute("documentsError", e.getMessage());
        }

        model.addAttribute("canCreateDocuments", canCreateDocuments);
        model.addAttribute("canEditDocuments", canEditDocuments);
        model.addAttribute("canDeleteDocuments", canDeleteDocuments);
        model.addAttribute("canViewVersions", canViewVersions);
        model.addAttribute("canReviewVersions", canReviewVersions);
        model.addAttribute("canManageUsers", canManageUsers);

        return "index";
    }
}