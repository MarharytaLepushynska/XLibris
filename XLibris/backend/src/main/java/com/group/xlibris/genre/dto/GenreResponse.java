package com.group.xlibris.genre.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Detailed representation of a genre response")
public record GenreResponse(
        @Schema(description = "Unique id of the genre", example = "550e8400-e29b-41d4-a716-446655449002")
        UUID id,

        @Schema(description = "Name of the genre", example = "Fantasy")
        String name
) {
}