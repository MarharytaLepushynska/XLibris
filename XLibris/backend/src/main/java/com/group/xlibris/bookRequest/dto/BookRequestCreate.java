package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.internal.CreateBookRequestCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Data to create a new book request")
public record BookRequestCreate(
        @Schema(description = "Unique id of the user requesting the book", example = "550e8400-e29b-41d4-a716-446655447000")
        @NotNull
        UUID requesterId,

        @Schema(description = "Desired duration of the loan in days", example = "14")
        @Min(1)
        @Max(360)
        int desiredDurationDays
) {
        public CreateBookRequestCommand toCommand(UUID bookId) {
                return new CreateBookRequestCommand(
                        bookId,
                        this.requesterId,
                        this.desiredDurationDays
                );
        }
}
