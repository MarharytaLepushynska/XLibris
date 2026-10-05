package com.group.xlibris.loan.dto;

import com.group.xlibris.loan.internal.CreateLoanCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Data to register a new book loan")
public record LoanRequest(
        @Schema(description = "Unique id of the book being borrowed", example = "550e8400-e29b-41d4-a716-446655445000")
        @NotNull
        UUID bookId,

        @Schema(description = "Unique id of the book owner", example = "550e8400-e29b-41d4-a716-446655446000")
        @NotNull
        UUID ownerId,

        @Schema(description = "Unique id of the user renting the book", example = "550e8400-e29b-41d4-a716-446655447000")
        @NotNull
        UUID renterId,

        @Schema(description = "Expected date and time for the book return (must be in the future)", example = "2026-11-01T12:00:00Z")
        @NotNull
        @Future
        Instant expectedReturnDate
) {
    public CreateLoanCommand toCommand() {
        return new CreateLoanCommand(
                this.bookId,
                this.ownerId,
                this.renterId,
                this.expectedReturnDate
        );
    }
}
