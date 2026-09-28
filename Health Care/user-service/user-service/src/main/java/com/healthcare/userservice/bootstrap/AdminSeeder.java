package com.healthcare.userservice.bootstrap;

import com.healthcare.userservice.entity.Role;
import com.healthcare.userservice.entity.User;
import com.healthcare.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Creates the first ADMIN so that the admin-only registration endpoint is reachable on a fresh database. */
@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class AdminSeeder {

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:admin@healthcare.local}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Bean
    public ApplicationRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByUsername(adminUsername)) {
                return;
            }
            if (adminPassword.isBlank()) {
                log.warn("No ADMIN_PASSWORD set - skipping admin seeding. "
                        + "Set the ADMIN_PASSWORD environment variable to bootstrap the first admin.");
                return;
            }

            userRepository.save(User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .email(adminEmail)
                    .role(Role.ADMIN)
                    .build());
            log.info("Seeded initial ADMIN user '{}'", adminUsername);
        };
    }
}
