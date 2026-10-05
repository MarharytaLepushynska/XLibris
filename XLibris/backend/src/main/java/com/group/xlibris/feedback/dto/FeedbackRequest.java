package com.group.xlibris.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Data to submit feedback for a completed book loan")
public record FeedbackRequest(

        @Schema(description = "Unique id of the completed book loan", example = "550e8400-e29b-41d4-a716-446655444000")
        @NotNull
        UUID loanId,

        @Schema(description = "Unique id of the user submitting the review", example = "550e8400-e29b-41d4-a716-446655446000")
        @NotNull
        UUID reviewerId,

        @Schema(description = "Unique id of the user being reviewed", example = "550e8400-e29b-41d4-a716-446655447000")
        @NotNull
        UUID reviewedUserId,

        @Schema(description = "Rating score from 1 to 5", example = "5")
        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,

        @Schema(description = "Comment describing the experience", example = "Great renter, returned the book on time and in perfect condition!")
        @NotBlank
        String comment

) {
}