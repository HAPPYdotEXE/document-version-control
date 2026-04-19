package com.project.practice.sap.config;

import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleDataInitializer {

    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            for (RoleType roleType : RoleType.values()) {
                roleRepository.findByRoleType(roleType)
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setRoleType(roleType);
                            return roleRepository.save(role);
                        });
            }
        };
    }
}