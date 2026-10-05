package com.group.xlibris.feedback.dto;

import com.group.xlibris.feedback.internal.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Detailed representation of a feedback response")
public record FeedbackResponse(
        @Schema(description = "Unique id of the feedback record", example = "550e8400-e29b-41d4-a716-446655444001")
        UUID id,

        @Schema(description = "Unique id of the completed book loan", example = "550e8400-e29b-41d4-a716-446655444000")
        UUID loanId,

        @Schema(description = "Unique id of the user submitting the review", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID reviewerId,

        @Schema(description = "Unique id of the user being reviewed", example = "550e8400-e29b-41d4-a716-446655447000")
        UUID reviewedUserId,

        @Schema(description = "Rating score from 1 to 5", example = "5")
        Integer rating,

        @Schema(description = "Comment describing the experience", example = "Great renter, returned the book on time and in perfect condition!")
        String comment,

        @Schema(description = "Timestamp when the feedback was created", example = "2026-10-05T12:00:00Z")
        Instant createdAt
) {

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getLoan().getId(),
                feedback.getReviewer().getId(),
                feedback.getReviewedUser().getId(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getCreatedAt()
        );
    }
}