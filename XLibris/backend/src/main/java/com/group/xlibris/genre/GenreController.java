package com.group.xlibris.genre;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import com.group.xlibris.genre.dto.GenreRequest;
import com.group.xlibris.genre.dto.GenreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
@Tag(name = "Book genres", description = "Endpoints for managing book genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    @Operation(summary = "Get all genres", description = "Retrieves a list of all book genres available")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of genres successfully retrieved")
    })
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        return ResponseEntity.ok(
                genreService.getAllGenres()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get genre by ID", description = "Retrieves detailed information about a specific genre by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Genre successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Genre not found")
    })
    public ResponseEntity<GenreResponse> getGenreById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                genreService.getGenreById(id)
        );
    }

    @PostMapping
    @Operation(summary = "Create genre", description = "Registers and normalizes a new book genre")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Genre successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error or no matching strategy found for genre name")
    })
    public ResponseEntity<GenreResponse> createGenre(
            @Validated(OnCreate.class)
            @RequestBody GenreRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(genreService.createGenre(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update genre", description = "Updates and normalizes an existing book genre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Genre successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation error or no matching strategy found"),
            @ApiResponse(responseCode = "404", description = "Genre not found")
    })
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable UUID id,
            @Validated(OnUpdate.class)
            @RequestBody GenreRequest request) {

        return ResponseEntity.ok(
                genreService.updateGenre(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete genre", description = "Removes a genre from the system by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Genre successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Genre not found")
    })
    public ResponseEntity<Void> deleteGenre(
            @PathVariable UUID id) {

        genreService.deleteGenre(id);

        return ResponseEntity.noContent().build();
    }
}