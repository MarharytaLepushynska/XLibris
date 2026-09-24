package com.group.xlibris.book.strategy;

import com.group.xlibris.book.internal.Book;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.internal.strategy.AvailableToBlockedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvailableToBlockedStrategyTest {

    private AvailableToBlockedStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AvailableToBlockedStrategy();
    }

    @Test
    void supports_shouldReturnTrueForAvailableToBlocked() {

        boolean result = strategy.supports(
                BookStatus.AVAILABLE,
                BookStatus.BLOCKED
        );

        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForInvalidTransition() {

        boolean result = strategy.supports(
                BookStatus.BORROWED,
                BookStatus.BLOCKED
        );

        assertThat(result).isFalse();
    }

    @Test
    void apply_shouldChangeBookStatusToBlocked() {

        Book book = new Book(
                UUID.randomUUID(),
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.AVAILABLE,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        strategy.apply(book);

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.BLOCKED);
    }
}