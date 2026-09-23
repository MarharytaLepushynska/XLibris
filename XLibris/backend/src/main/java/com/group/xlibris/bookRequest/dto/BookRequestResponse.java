package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.internal.BookRequestEntity;
import com.group.xlibris.bookRequest.BookRequestStatus;

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
) {
    public static BookRequestResponse from(BookRequestEntity entity) {
        return new BookRequestResponse(
                entity.getId(),
                entity.getBookId(),
                entity.getRequesterId(),
                entity.getOwnerId(),
                entity.getDesiredDurationDays(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt()
        );
    }
}
