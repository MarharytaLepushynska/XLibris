package com.group.xlibris.loan;

import java.time.Instant;
import java.util.UUID;

public record LoanReturnedEvent(
        UUID loanId,
        UUID bookId,
        UUID ownerId,
        UUID renterId,
        Instant returnDate
) {
}
