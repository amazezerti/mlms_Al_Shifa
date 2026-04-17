package com.alshifa.mlms_al_shifa.config;

import com.alshifa.mlms_al_shifa.model.Role;
import com.alshifa.mlms_al_shifa.model.User;
import com.alshifa.mlms_al_shifa.repository.RoleRepository;
import com.alshifa.mlms_al_shifa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository     userRepository;
    private final RoleRepository     roleRepository;
    private final PasswordEncoder    passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.fullname}")
    private String adminFullName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {

        // Only create admin if not already present
        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user already exists — skipping seed.");
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ROLE_ADMIN not found. Check V2 migration."));

        User admin = User.builder()
                .fullName(adminFullName)
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword)) // BCrypt applied HERE
                .email(adminEmail)
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Admin user '{}' created successfully.", adminUsername);
    }
}