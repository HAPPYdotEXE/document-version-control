package com.project.practice.sap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // the active security rules / registered in the Spring Application context upon starting the app
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // (csrf)by default requires secret token from the user on every state-changing request
            .csrf(csrf -> csrf.disable())
            // disable another security measure in order to allow h2 console to render ---> will be enabled later or scoped only to /h2-console
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            // allow all requests — ROLE BASED Security will be added later with @PreAuthorize
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    // BCrypt password encoder — used to hash passwords upon user creation
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
