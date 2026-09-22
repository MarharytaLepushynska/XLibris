package com.group.xlibris.genre.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiWordGenreStrategyTest {

    private MultiWordGenreStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MultiWordGenreStrategy();
    }

    @Test
    void supports_shouldReturnTrueForMultiWordGenre() {
        boolean result = strategy.supports("Dark Fantasy");

        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForSingleWordGenre() {
        boolean result = strategy.supports("Fantasy");

        assertThat(result).isFalse();
    }

    @Test
    void normalize_shouldFormatMultiWordGenre() {
        String result = strategy.normalize("  dark   fantasy  ");

        assertThat(result).isEqualTo("Dark Fantasy");
    }
}