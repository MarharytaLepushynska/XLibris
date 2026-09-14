package com.group.xlibris.feedback.dto;

import com.group.xlibris.feedback.entity.Feedback;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(
        UUID id,
        UUID loanId,
        UUID reviewerId,
        UUID reviewedUserId,
        Integer rating,
        String comment,
        Instant createdAt
) {

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.id(),
                feedback.loanId(),
                feedback.reviewerId(),
                feedback.reviewedUserId(),
                feedback.rating(),
                feedback.comment(),
                feedback.createdAt()
        );
    }
}