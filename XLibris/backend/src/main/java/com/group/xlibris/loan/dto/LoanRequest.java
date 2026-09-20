package com.group.xlibris.loan.dto;

import com.group.xlibris.loan.command.CreateLoanCommand;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record LoanRequest(
        @NotNull
        UUID bookId,

        @NotNull
        UUID ownerId,

        @NotNull
        UUID renterId,

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
