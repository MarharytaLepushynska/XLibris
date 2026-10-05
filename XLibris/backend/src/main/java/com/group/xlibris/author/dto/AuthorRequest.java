package com.group.xlibris.author.dto;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Data to create or update a book author")
public record AuthorRequest(

        @Schema(description = "Unique id of the author (must be null on create, required on update)", example = "550e8400-e29b-41d4-a716-446655448001")
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @Schema(description = "Full name of the author", example = "J.K. Rowling")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 255, groups = {OnCreate.class, OnUpdate.class})
        String name
) {
}