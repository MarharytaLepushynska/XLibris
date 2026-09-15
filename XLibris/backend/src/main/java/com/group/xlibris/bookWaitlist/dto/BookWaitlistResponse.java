package com.group.xlibris.bookWaitlist.dto;

import com.group.xlibris.bookWaitlist.enums.BookWaitlistStatus;

import java.time.Instant;
import java.util.UUID;

public record BookWaitlistResponse(
        UUID id,
        UUID bookId,
        UUID userId,
        int position,
        Instant joinedAt,
        BookWaitlistStatus status,
        Instant notifiedAt,
        Instant responseDeadline
) {}
