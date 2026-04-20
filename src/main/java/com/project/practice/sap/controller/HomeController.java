package com.project.practice.sap.controller;

import com.project.practice.sap.dto.DocumentResponseDTO;
import com.project.practice.sap.model.User;
import com.project.practice.sap.repository.DocumentRepository;
import com.project.practice.sap.repository.UserRepository;
import com.project.practice.sap.service.DocumentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    public HomeController(UserRepository userRepository, DocumentService documentService, DocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.documentService = documentService;
        this.documentRepository = documentRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        boolean isLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isLoggedIn);

        boolean canCreateDocuments = false;
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
        model.addAttribute("canViewVersions", canViewVersions);
        model.addAttribute("canReviewVersions", canReviewVersions);
        model.addAttribute("canManageUsers", canManageUsers);

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
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        model.addAttribute("user", user);
        addUserInfo(authentication, model);
        return "profile";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-documents")
    public String myDocuments(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (user != null) {
            List<DocumentResponseDTO> myDocuments = documentRepository.findByCreatedById(user.getId())
                    .stream()
                    .map(document -> documentService.getDocumentById(document.getId()))
                    .toList();

            model.addAttribute("documents", myDocuments);
        } else {
            model.addAttribute("documents", Collections.emptyList());
        }

        addUserInfo(authentication, model);
        return "my-documents";
    }

    private void addUserInfo(Authentication authentication, Model model) {
        boolean isLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUsername", user.getUsername());
                model.addAttribute("currentEmail", user.getEmail());

                String currentRole =
                        user.getRoles() != null && !user.getRoles().isEmpty()
                                ? user.getRoles().get(0).getRoleType().name()
                                : "GUEST";

                model.addAttribute("currentRole", currentRole);
            }
        }
    }
}