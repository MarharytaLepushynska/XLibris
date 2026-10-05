package com.group.xlibris.book.dto;

import com.group.xlibris.book.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.modulith.NamedInterface;

import java.util.UUID;

@NamedInterface("api")
@Schema(description = "Detailed representation of a book response")
public record BookResponse(
        @Schema(description = "Unique id of the book", example = "550e8400-e29b-41d4-a716-446655445000")
        UUID id,

        @Schema(description = "Title of the book", example = "Harry Potter and the Philosopher's Stone")
        String title,

        @Schema(description = "Detailed description of the book", example = "A fantasy novel about an orphaned boy who discovers he is a wizard and attends Hogwarts School of Witchcraft and Wizardry")
        String description,

        @Schema(description = "URL to the book's cover photo", example = "https://example.com/images/potter1.jpg")
        String photoURL,

        @Schema(description = "Current availability status of the book", example = "AVAILABLE")
        BookStatus status,

        @Schema(description = "Unique id of the book owner", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID ownerId,

        @Schema(description = "Unique id of the book author", example = "550e8400-e29b-41d4-a716-446655448001")
        UUID authorId,

        @Schema(description = "Unique id of the book genre", example = "550e8400-e29b-41d4-a716-446655449002")
        UUID genreId
) {
}
