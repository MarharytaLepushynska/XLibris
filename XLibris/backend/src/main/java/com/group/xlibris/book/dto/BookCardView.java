package com.group.xlibris.book.dto;

import com.group.xlibris.book.BookStatus;

import java.util.UUID;

public record BookCardView(
        UUID id,
        String title,
        String description,
        String genreName,
        String authorName,
        String ownerName,
        BookStatus status
) {
}
