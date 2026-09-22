package com.group.xlibris.book.strategy;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BorrowedToAvailableStrategyTest {

    private BorrowedToAvailableStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BorrowedToAvailableStrategy();
    }

    @Test
    void supports_shouldReturnTrueForBorrowedToAvailable() {

        boolean result = strategy.supports(
                BookStatus.BORROWED,
                BookStatus.AVAILABLE
        );

        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForInvalidTransition() {

        boolean result = strategy.supports(
                BookStatus.AVAILABLE,
                BookStatus.BLOCKED
        );

        assertThat(result).isFalse();
    }

    @Test
    void apply_shouldChangeBookStatusToAvailable() {

        Book book = new Book(
                UUID.randomUUID(),
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.BORROWED,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        strategy.apply(book);

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.AVAILABLE);
    }
}