package com.project.practice.sap.controller;

import com.project.practice.sap.model.User;
import com.project.practice.sap.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
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
                model.addAttribute(
                        "currentRole",
                        user.getRoles() != null && !user.getRoles().isEmpty()
                                ? user.getRoles().get(0).getRoleType().name()
                                : "User"
                );
            }
        }

        return "index";
    }
}
