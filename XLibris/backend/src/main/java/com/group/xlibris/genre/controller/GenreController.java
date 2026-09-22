package com.group.xlibris.genre.controller;

import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import com.group.xlibris.genre.dto.GenreRequest;
import com.group.xlibris.genre.dto.GenreResponse;
import com.group.xlibris.genre.service.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        return ResponseEntity.ok(
                genreService.getAllGenres()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                genreService.getGenreById(id)
        );
    }

    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(
            @Validated(OnCreate.class)
            @RequestBody GenreRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(genreService.createGenre(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable UUID id,
            @Validated(OnUpdate.class)
            @RequestBody GenreRequest request) {

        return ResponseEntity.ok(
                genreService.updateGenre(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(
            @PathVariable UUID id) {

        genreService.deleteGenre(id);

        return ResponseEntity.noContent().build();
    }
}