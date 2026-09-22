package com.group.xlibris.genre.repository;

import com.group.xlibris.genre.entity.Genre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenreRepository {

    Genre save(Genre genre);

    Optional<Genre> findById(UUID id);

    List<Genre> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);
}