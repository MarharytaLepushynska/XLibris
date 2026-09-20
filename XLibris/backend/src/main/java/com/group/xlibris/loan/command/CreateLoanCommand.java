package com.group.xlibris.loan.command;

import java.time.Instant;
import java.util.UUID;

public record CreateLoanCommand(
        UUID bookId,
        UUID ownerId,
        UUID renterId,
        Instant expectedReturnDate
) {
}
