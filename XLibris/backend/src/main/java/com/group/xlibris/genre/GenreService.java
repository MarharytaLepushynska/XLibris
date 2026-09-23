package com.group.xlibris.genre;

import java.util.List;
import java.util.UUID;

public interface GenreService {

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(UUID id);

    GenreResponse createGenre(GenreRequest request);

    GenreResponse updateGenre(UUID id, GenreRequest request);

    void deleteGenre(UUID id);
}