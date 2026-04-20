package com.project.practice.sap.service;

import com.project.practice.sap.model.User;
import com.project.practice.sap.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SecurityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("LOGIN TRY USERNAME = " + username);

        User dbUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = dbUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleType().name()))
                .collect(java.util.stream.Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(dbUser.getUsername())
                .password(dbUser.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
