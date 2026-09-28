package com.group.xlibris.book.strategy;

import com.group.xlibris.author.Author;
import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.internal.strategy.BlockedToAvailableStrategy;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlockedToAvailableStrategyTest {

    private BlockedToAvailableStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BlockedToAvailableStrategy();
    }

    @Test
    void supports_shouldReturnTrueForBlockedToAvailable() {

        boolean result = strategy.supports(
                BookStatus.BLOCKED,
                BookStatus.AVAILABLE
        );

        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForInvalidTransition() {

        boolean result = strategy.supports(
                BookStatus.BORROWED,
                BookStatus.AVAILABLE
        );

        assertThat(result).isFalse();
    }

    @Test
    void apply_shouldChangeBookStatusToAvailable() {

        User user = new User(UUID.randomUUID(), "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);

        Author author = new Author(UUID.randomUUID(), "JK Rowling");

        Genre genre = new Genre(UUID.randomUUID(), "Horror");

        Book book = new Book(
                UUID.randomUUID(),
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.BLOCKED,
                user,
                author,
                genre
        );

        strategy.apply(book);

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.AVAILABLE);
    }
}