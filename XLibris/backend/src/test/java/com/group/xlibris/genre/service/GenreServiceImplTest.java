package com.group.xlibris.genre.service;

import com.group.xlibris.genre.GenreRequest;
import com.group.xlibris.genre.GenreResponse;
import com.group.xlibris.genre.entity.Genre;
import com.group.xlibris.genre.GenreNotFoundException;
import com.group.xlibris.genre.internal.GenreServiceImpl;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.genre.internal.strategy.GenreStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreStrategy genreStrategy;

    private GenreServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GenreServiceImpl(
                genreRepository,
                List.of(genreStrategy)
        );
    }

    @Test
    void getAllGenres_shouldReturnGenres() {
        Genre genre1 = new Genre(
                UUID.randomUUID(),
                "Fantasy"
        );

        Genre genre2 = new Genre(
                UUID.randomUUID(),
                "Horror"
        );

        when(genreRepository.findAll())
                .thenReturn(List.of(genre1, genre2));

        List<GenreResponse> result = service.getAllGenres();

        assertThat(result)
                .hasSize(2)
                .extracting(GenreResponse::name)
                .containsExactly("Fantasy", "Horror");

        verify(genreRepository).findAll();
    }

    @Test
    void getGenreById_shouldReturnGenre() {
        UUID id = UUID.randomUUID();

        Genre genre = new Genre(id, "Fantasy");

        when(genreRepository.findById(id))
                .thenReturn(Optional.of(genre));

        GenreResponse result = service.getGenreById(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Fantasy");

        verify(genreRepository).findById(id);
    }

    @Test
    void getGenreById_shouldThrowExceptionWhenGenreNotFound() {
        UUID id = UUID.randomUUID();

        when(genreRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGenreById(id))
                .isInstanceOf(GenreNotFoundException.class);

        verify(genreRepository).findById(id);
    }

    @Test
    void createGenre_shouldUseStrategyAndSaveGenre() {
        GenreRequest request = new GenreRequest(
                null,
                "fantasy"
        );

        when(genreStrategy.supports("fantasy"))
                .thenReturn(true);

        when(genreStrategy.normalize("fantasy"))
                .thenReturn("Fantasy");

        when(genreRepository.save(any(Genre.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GenreResponse result = service.createGenre(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Fantasy");

        verify(genreStrategy).supports("fantasy");
        verify(genreStrategy).normalize("fantasy");
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void updateGenre_shouldUseStrategyAndUpdateGenre() {
        UUID id = UUID.randomUUID();

        Genre existingGenre = new Genre(
                id,
                "Fantasy"
        );

        GenreRequest request = new GenreRequest(
                id,
                "dark fantasy"
        );

        when(genreRepository.findById(id))
                .thenReturn(Optional.of(existingGenre));

        when(genreStrategy.supports("dark fantasy"))
                .thenReturn(true);

        when(genreStrategy.normalize("dark fantasy"))
                .thenReturn("Dark Fantasy");

        when(genreRepository.save(existingGenre))
                .thenReturn(existingGenre);

        GenreResponse result = service.updateGenre(id, request);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Dark Fantasy");

        verify(genreRepository).findById(id);
        verify(genreStrategy).supports("dark fantasy");
        verify(genreStrategy).normalize("dark fantasy");
        verify(genreRepository).save(existingGenre);
    }

    @Test
    void updateGenre_shouldThrowExceptionWhenGenreNotFound() {
        UUID id = UUID.randomUUID();

        GenreRequest request = new GenreRequest(
                id,
                "Fantasy"
        );

        when(genreRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateGenre(id, request))
                .isInstanceOf(GenreNotFoundException.class);

        verify(genreRepository).findById(id);
        verify(genreRepository, never()).save(any(Genre.class));
        verify(genreStrategy, never()).supports(any(String.class));
    }

    @Test
    void deleteGenre_shouldDeleteGenre() {
        UUID id = UUID.randomUUID();

        when(genreRepository.existsById(id))
                .thenReturn(true);

        service.deleteGenre(id);

        verify(genreRepository).existsById(id);
        verify(genreRepository).deleteById(id);
    }

    @Test
    void deleteGenre_shouldThrowExceptionWhenGenreNotFound() {
        UUID id = UUID.randomUUID();

        when(genreRepository.existsById(id))
                .thenReturn(false);

        assertThatThrownBy(() -> service.deleteGenre(id))
                .isInstanceOf(GenreNotFoundException.class);

        verify(genreRepository).existsById(id);
        verify(
                genreRepository,
                never()
        ).deleteById(any(UUID.class));
    }

    @Test
    void createGenre_shouldThrowExceptionWhenNoStrategySupportsGenre() {
        GenreRequest request = new GenreRequest(
                null,
                "fantasy"
        );

        when(genreStrategy.supports("fantasy"))
                .thenReturn(false);

        assertThatThrownBy(() -> service.createGenre(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(genreStrategy).supports("fantasy");
        verify(genreStrategy, never()).normalize(any(String.class));
        verify(genreRepository, never()).save(any(Genre.class));
    }
}