package com.group.xlibris.genre.internal;

import com.group.xlibris.genre.GenreRequest;
import com.group.xlibris.genre.GenreResponse;
import com.group.xlibris.genre.entity.Genre;
import com.group.xlibris.genre.GenreNotFoundException;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.genre.GenreService;
import com.group.xlibris.genre.internal.strategy.GenreStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final List<GenreStrategy> strategies;

    public GenreServiceImpl(
            GenreRepository genreRepository,
            List<GenreStrategy> strategies
    ) {
        this.genreRepository = genreRepository;
        this.strategies = strategies;
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GenreResponse getGenreById(UUID id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new GenreNotFoundException(id));

        return toResponse(genre);
    }

    @Override
    public GenreResponse createGenre(GenreRequest request) {
        GenreStrategy strategy = findStrategy(request.name());

        String normalizedName = strategy.normalize(request.name());

        Genre genre = new Genre(
                UUID.randomUUID(),
                normalizedName
        );

        Genre savedGenre = genreRepository.save(genre);

        return toResponse(savedGenre);
    }

    @Override
    public GenreResponse updateGenre(UUID id, GenreRequest request) {
        Genre existingGenre = genreRepository.findById(id)
                .orElseThrow(() -> new GenreNotFoundException(id));

        GenreStrategy strategy = findStrategy(request.name());

        String normalizedName = strategy.normalize(request.name());

        existingGenre.setName(normalizedName);

        Genre updatedGenre = genreRepository.save(existingGenre);

        return toResponse(updatedGenre);
    }

    @Override
    public void deleteGenre(UUID id) {
        if (!genreRepository.existsById(id)) {
            throw new GenreNotFoundException(id);
        }

        genreRepository.deleteById(id);
    }

    private GenreStrategy findStrategy(String genreName) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(genreName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No strategy found for genre: " + genreName
                        )
                );
    }

    private GenreResponse toResponse(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName()
        );
    }
}