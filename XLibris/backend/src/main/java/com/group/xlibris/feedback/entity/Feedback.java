package com.group.xlibris.feedback.entity;

import java.time.Instant;
import java.util.UUID;

public record Feedback(
        UUID id,
        UUID loanId,
        UUID reviewerId,
        UUID reviewedUserId,
        Integer rating,
        String comment,
        Instant createdAt
) {
}