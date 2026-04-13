package com.project.practice.sap.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        if (token != null) {
            try {
                UsernamePasswordAuthenticationToken authToken = buildAuthToken(token, request);
                if (authToken != null) {
                    // Place the authenticated user into the SecurityContext for this request
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                System.out.println("JWT filter exception: " + e.getClass().getSimpleName() + " — " + e.getMessage());
            }
        }
        //passes the request to the next filter as authenticated if no error has been caught -> if caught it passes with no auth
        filterChain.doFilter(request, response);
    }

    // Reads the Authorization header and returns the raw token string or null for non-authenticated requests
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        //non-authenticated request -> passes the request to the next filer as unauthenticated
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        // Strip the "Bearer " prefix to get the raw token string
        return authHeader.substring(7);
    }

    // Validates the token and builds the authentication object; returns null if context is already filled or token is invalid
    private UsernamePasswordAuthenticationToken buildAuthToken(String token, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(token);
        // fills in the security auth context if its not present yet
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return null;
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(token, userDetails)) {
            return null;
        }
        // Build the authentication object
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); // fetch the roles
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }
}
