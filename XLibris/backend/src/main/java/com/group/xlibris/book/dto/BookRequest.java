package com.group.xlibris.book.dto;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Data to create or update a book")
public record BookRequest(

        @Schema(description = "Unique id of the book (must be null on create, required on update)", example = "550e8400-e29b-41d4-a716-446655445000")
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @Schema(description = "Title of the book", example = "Harry Potter and the Philosopher's Stone")
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 100, groups = {OnCreate.class, OnUpdate.class})
        String title,

        @Schema(description = "Detailed description of the book", example = "A fantasy novel about an orphaned boy who discovers he is a wizard and attends Hogwarts School of Witchcraft and Wizardry")
        @Size(max = 2000, groups = {OnCreate.class, OnUpdate.class})
        String description,

        @Schema(description = "URL to the book's cover photo", example = "https://example.com/images/potter1.jpg")
        String photoURL,

        @Schema(description = "Unique id of the book owner", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID ownerId,

        @Schema(description = "Unique id of the book author", example = "550e8400-e29b-41d4-a716-446655448001")
        UUID authorId,

        @Schema(description = "Unique id of the book genre", example = "550e8400-e29b-41d4-a716-446655449002")
        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        UUID genreId

) {
}
