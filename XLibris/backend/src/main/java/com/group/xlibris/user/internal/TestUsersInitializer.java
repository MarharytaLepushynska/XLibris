package com.group.xlibris.user.internal;

import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Component
@Profile("dev")
public class TestUsersInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestUsersInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (Role role : Role.values()) {
            String email = role.name().toLowerCase(Locale.ROOT) + "@xlibris.com";

            if (userRepository.findByEmail(email).isEmpty()) {
                userRepository.save(new User(
                        UUID.randomUUID(),
                        "Test" + role.name(),
                        "Kyiv",
                        null,
                        email,
                        "+380000000000",
                        Instant.now(),
                        role,
                        passwordEncoder.encode("Password123"),
                        0.0,
                        0.0,
                        0,
                        0,
                        0
                ));
            }
        }
    }
}
