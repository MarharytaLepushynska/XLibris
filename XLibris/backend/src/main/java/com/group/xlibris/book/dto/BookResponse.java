package com.group.xlibris.book.dto;

import com.group.xlibris.book.BookStatus;
import org.springframework.modulith.NamedInterface;

import java.util.UUID;

@NamedInterface("api")
public record BookResponse(
        UUID id,
        String title,
        String description,
        String photoURL,
        BookStatus status,
        UUID ownerId,
        UUID authorId,
        UUID genreId
) {
}
