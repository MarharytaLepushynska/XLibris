package com.group.xlibris.book.strategy;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvailableToBorrowedStrategyTest {

    private AvailableToBorrowedStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AvailableToBorrowedStrategy();
    }

    @Test
    void supports_shouldReturnTrueForAvailableToBorrowed() {

        boolean result = strategy.supports(
                BookStatus.AVAILABLE,
                BookStatus.BORROWED
        );

        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForInvalidTransition() {

        boolean result = strategy.supports(
                BookStatus.BLOCKED,
                BookStatus.BORROWED
        );

        assertThat(result).isFalse();
    }

    @Test
    void apply_shouldChangeBookStatusToBorrowed() {

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
                .isEqualTo(BookStatus.BORROWED);
    }
}