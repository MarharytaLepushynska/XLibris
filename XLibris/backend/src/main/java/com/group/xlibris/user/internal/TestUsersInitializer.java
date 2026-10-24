package com.group.xlibris.user.internal;

import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("dev")
public class TestUsersInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public TestUsersInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (Role role: Role.values()) {
            String email = role.name().toLowerCase(Locale.ROOT) + "@xlibris.com";
            if (userRepository.findByEmail(email).isEmpty()) {
                userRepository.save(new User(UUID.randomUUID(), "Test" + role.name(),
                        "Kyiv", null, email, "+380000000000", Instant.now(),
                        role, passwordEncoder.encode("Password123"), 0.0, 0.0, 0, 0, 0));
            }
        }
        Optional<User> users = userRepository.findByEmail("user@xlibris.com");
        if(!users.isEmpty()) {
            User user = users.get();
            for (int i = 0; i < 4; i++) {
                bookRepository.save(new Book(UUID.randomUUID(), "Book " + i, "interesting book",
                        null, BookStatus.AVAILABLE, user, null, null));
            }
        }
    }
}
