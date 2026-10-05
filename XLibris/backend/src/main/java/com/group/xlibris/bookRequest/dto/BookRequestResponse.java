package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.internal.BookRequestEntity;
import com.group.xlibris.common.BookRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Detailed representation of a book request response")
public record BookRequestResponse(
        @Schema(description = "Unique id of the book request", example = "550e8400-e29b-41d4-a716-446655444000")
        UUID id,

        @Schema(description = "Unique id of the requested book", example = "550e8400-e29b-41d4-a716-446655445000")
        UUID bookId,

        @Schema(description = "Unique id of the user who requested the book", example = "550e8400-e29b-41d4-a716-446655447000")
        UUID requesterId,

        @Schema(description = "Unique id of the book owner", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID ownerId,

        @Schema(description = "Desired duration of the loan in days", example = "14")
        int desiredDurationDays,

        @Schema(description = "Current status of the book request", example = "PENDING")
        BookRequestStatus status,

        @Schema(description = "Timestamp when the request was created", example = "2026-10-05T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when the request status was responded to", example = "2026-10-05T11:30:00Z")
        Instant respondedAt
) {
    public static BookRequestResponse from(BookRequestEntity entity) {
        return new BookRequestResponse(
                entity.getId(),
                entity.getBook().getId(),
                entity.getRequester().getId(),
                entity.getOwner().getId(),
                entity.getDesiredDurationDays(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt()
        );
    }
}
