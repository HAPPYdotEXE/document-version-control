package com.project.practice.sap.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.project.practice.sap.model.Role;
import com.project.practice.sap.model.enums.RoleType;
import com.project.practice.sap.repository.RoleRepository;

@Configuration
public class RoleDataInitializer {

    // the idea is to have 1-4 predefined roles at the start of the application
    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            for (RoleType roleType : RoleType.values()) {
                if (roleRepository.findByRoleType(roleType).isEmpty()) {
                    Role role = new Role();
                    role.setRoleType(roleType);
                    roleRepository.save(role);
                }
            }
        };
    }
}
