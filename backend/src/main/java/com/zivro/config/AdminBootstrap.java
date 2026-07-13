package com.zivro.config;

import com.zivro.domain.Role;
import com.zivro.domain.User;
import com.zivro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(100)
public class AdminBootstrap implements ApplicationRunner {

    private final BootstrapAdminProperties bootstrapAdminProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapAdminProperties.shouldRun()) {
            return;
        }
        String email = bootstrapAdminProperties.email().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.debug("Bootstrap admin skipped: user already exists for {}", email);
            return;
        }
        String name =
                bootstrapAdminProperties.name() != null && !bootstrapAdminProperties.name().isBlank()
                        ? bootstrapAdminProperties.name().trim()
                        : "Admin";
        User admin =
                User.builder()
                        .email(email.toLowerCase())
                        .password(passwordEncoder.encode(bootstrapAdminProperties.password()))
                        .name(name)
                        .role(Role.ADMIN)
                        .build();
        userRepository.save(admin);
        log.info("Created bootstrap ADMIN user for {}", email);
    }
}
