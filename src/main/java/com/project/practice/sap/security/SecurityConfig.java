package com.project.practice.sap.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            // STATELESS — Spring will never create an HttpSession; every request must carry its own JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                .requestMatchers("/api/v1/audit-logs/**").hasRole("ADMIN")

                .requestMatchers("/api/v1/users/me").authenticated()
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/v1/documents").hasAnyRole("AUTHOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/documents/**").hasAnyRole("AUTHOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/documents/**").hasAnyRole("AUTHOR", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/v1/documents/*/versions").hasAnyRole("AUTHOR", "ADMIN")

                .requestMatchers(HttpMethod.PUT, "/api/v1/documents/*/versions/*/approve").hasAnyRole("REVIEWER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/documents/*/versions/*/reject").hasAnyRole("REVIEWER", "ADMIN")

                .anyRequest().authenticated()
            )
            .authenticationProvider(daoAuthenticationProvider())
            // Insert our JWT filter before Spring's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Wires together our UserDetailsService and BCrypt encoder — this is what authenticate() calls internally
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Exposed as a bean so AuthServiceImpl can call authenticate() directly
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
