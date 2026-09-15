package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.enums.BookRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record BookRequestResponse(
        UUID id,
        UUID bookId,
        UUID requesterId,
        UUID ownerId,
        int desiredDurationDays,
        BookRequestStatus status,
        Instant createdAt,
        Instant respondedAt
) {}
