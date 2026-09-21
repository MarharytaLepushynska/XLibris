package com.group.xlibris.book.dto;

import com.group.xlibris.book.enums.BookStatus;

import java.util.UUID;

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
