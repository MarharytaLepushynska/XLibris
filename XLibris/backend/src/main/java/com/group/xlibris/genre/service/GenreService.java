package com.group.xlibris.genre.service;

import com.group.xlibris.genre.dto.GenreRequest;
import com.group.xlibris.genre.dto.GenreResponse;

import java.util.List;
import java.util.UUID;

public interface GenreService {

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(UUID id);

    GenreResponse createGenre(GenreRequest request);

    GenreResponse updateGenre(UUID id, GenreRequest request);

    void deleteGenre(UUID id);
}