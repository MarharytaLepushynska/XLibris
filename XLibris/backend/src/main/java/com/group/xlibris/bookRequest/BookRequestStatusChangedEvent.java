package com.group.xlibris.bookRequest;

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
