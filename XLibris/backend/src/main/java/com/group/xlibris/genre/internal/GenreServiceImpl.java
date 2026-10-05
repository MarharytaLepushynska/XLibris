package com.group.xlibris.genre.internal;

import com.group.xlibris.genre.Genre;
import com.group.xlibris.genre.dto.GenreRequest;
import com.group.xlibris.genre.dto.GenreResponse;
import com.group.xlibris.genre.GenreNotFoundException;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.genre.GenreService;
import com.group.xlibris.genre.internal.strategy.GenreStrategy;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class GenreServiceImpl implements GenreService {

    private static final Logger log = LoggerFactory.getLogger(GenreServiceImpl.class);

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

        log.debug("Fetching all genres");

        return genreRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GenreResponse getGenreById(UUID id) {

        log.debug("Finding genre by id={}", id);

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

        log.info("Genre with id={} was created", savedGenre.getId());

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

        log.info("Genre information with id={} was updated", updatedGenre.getId());

        return toResponse(updatedGenre);
    }

    @Override
    @Transactional
    public void deleteGenre(UUID id) {
        if (!genreRepository.existsById(id)) {
            throw new GenreNotFoundException(id);
        }

        genreRepository.deleteById(id);

        log.info("Genre with id={} was deleted", id);
    }

    @Override
    public Genre getEntityById(UUID id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new GenreNotFoundException(id));
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