package com.group.xlibris.loan.internal;

import org.springframework.modulith.NamedInterface;

import java.time.Instant;
import java.util.UUID;

@NamedInterface(value = "api")
public record CreateLoanCommand(
        UUID bookId,
        UUID ownerId,
        UUID renterId,
        Instant expectedReturnDate
) {
}
