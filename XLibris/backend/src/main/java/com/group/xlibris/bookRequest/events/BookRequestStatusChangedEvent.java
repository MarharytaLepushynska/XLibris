package com.group.xlibris.bookRequest.events;

import com.group.xlibris.bookRequest.enums.BookRequestStatus;

import java.util.UUID;

public record BookRequestStatusChangedEvent(
        UUID requestId,
        UUID bookId,
        UUID requesterId,
        UUID ownerId,
        int desiredDurationDays,
        BookRequestStatus previousStatus,
        BookRequestStatus newStatus
) {}
