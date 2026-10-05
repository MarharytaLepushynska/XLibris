package com.group.xlibris.genre.dto;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Data to create or update a book genre")
public record GenreRequest(

        @Schema(description = "Unique id of the genre (must be null on create, required on update)", example = "550e8400-e29b-41d4-a716-446655449002")
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @Schema(description = "Name of the genre", example = "Fantasy")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 50, groups = {OnCreate.class, OnUpdate.class})
        String name
) {
}