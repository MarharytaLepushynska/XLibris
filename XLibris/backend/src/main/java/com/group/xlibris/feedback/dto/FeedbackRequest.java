package com.group.xlibris.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FeedbackRequest(

        @NotNull
        UUID loanId,

        @NotNull
        UUID reviewerId,

        @NotNull
        UUID reviewedUserId,

        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,

        @NotBlank
        String comment

) {
}