package com.group.xlibris.loan.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.Instant;
import java.util.UUID;

public record LoanRequest(
        @Null
        UUID id,

        @NotNull
        UUID bookId,

        @NotNull
        UUID ownerId,

        @NotNull
        UUID renterId,

        @NotNull
        @Future
        Instant expectedReturnDate,

        @Null
        Instant actualReturnDate
) {
}
