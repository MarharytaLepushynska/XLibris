package com.group.xlibris.genre.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SingleWordGenreStrategyTest {

    private SingleWordGenreStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SingleWordGenreStrategy();
    }

    @Test
    void supports_shouldReturnTrueForSingleWordGenre() {
        boolean result = strategy.supports("Fantasy");

        assertThat(result).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForMultiWordGenre() {
        boolean result = strategy.supports("Dark Fantasy");

        assertThat(result).isFalse();
    }

    @Test
    void normalize_shouldFormatSingleWordGenre() {
        String result = strategy.normalize("  fAnTaSy  ");

        assertThat(result).isEqualTo("Fantasy");
    }
}