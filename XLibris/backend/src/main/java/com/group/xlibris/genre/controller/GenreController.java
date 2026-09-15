package com.group.xlibris.genre.controller;

import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import com.group.xlibris.genre.dto.GenreRequest;
import com.group.xlibris.genre.dto.GenreResponse;
import com.group.xlibris.genre.entity.Genre;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/genres")
public class GenreController {

    private final Map<UUID, Genre> genres = new ConcurrentHashMap<>();

    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres() {

        List<GenreResponse> response = genres.values()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(
            @PathVariable UUID id
    ) {

        Genre genre = genres.get(id);

        if (genre == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(genre));
    }

    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(
            @Validated(OnCreate.class) @RequestBody GenreRequest request
    ) {

        UUID id = UUID.randomUUID();

        Genre genre = new Genre(
                id,
                request.name()
        );

        genres.put(id, genre);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(genre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable UUID id,
            @Validated(OnUpdate.class) @RequestBody GenreRequest request
    ) {

        if (!genres.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        Genre updatedGenre = new Genre(
                id,
                request.name()
        );

        genres.put(id, updatedGenre);

        return ResponseEntity.ok(toResponse(updatedGenre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(
            @PathVariable UUID id
    ) {

        if (!genres.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        genres.remove(id);

        return ResponseEntity.noContent().build();
    }

    private GenreResponse toResponse(Genre genre) {

        return new GenreResponse(
                genre.getId(),
                genre.getName()
        );
    }
}