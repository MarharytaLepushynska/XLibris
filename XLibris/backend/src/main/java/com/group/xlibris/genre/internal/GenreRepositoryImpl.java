package com.group.xlibris.genre.internal;

import com.group.xlibris.genre.entity.Genre;
import com.group.xlibris.genre.GenreRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GenreRepositoryImpl implements GenreRepository {

    private final Map<UUID, Genre> genres = new ConcurrentHashMap<>();

    @Override
    public Genre save(Genre genre) {
        genres.put(genre.getId(), genre);
        return genre;
    }

    @Override
    public Optional<Genre> findById(UUID id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public List<Genre> findAll() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public boolean existsById(UUID id) {
        return genres.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        genres.remove(id);
    }
}