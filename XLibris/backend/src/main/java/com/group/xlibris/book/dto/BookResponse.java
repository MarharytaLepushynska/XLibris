package com.group.xlibris.book.dto;

import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String description,
        String photoURL,
        String status,
        UUID ownerId,
        UUID authorId,
        UUID genreId
) {
}
