package com.project.practice.sap.controller;

import com.project.practice.sap.dto.LoginRequest;
import com.project.practice.sap.exception.DuplicateResourceException;
import com.project.practice.sap.model.User;
import com.project.practice.sap.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController {

    private final AuthService authService;

    public AuthPageController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        model.addAttribute("mode", "login");
        return "login_page";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        model.addAttribute("mode", "register");
        return "login_page";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "register");
            return "login_page";
        }

        try {
            authService.register(user);
        } catch (DuplicateResourceException | IllegalArgumentException ex) {
            model.addAttribute("registerError", ex.getMessage());
            model.addAttribute("mode", "register");
            return "login_page";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Registration successful. Your account was created with READER access."
        );
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequest request,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        try {
            String token = authService.login(request).token();

            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loginError", "Invalid username or password.");
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login";
    }
}