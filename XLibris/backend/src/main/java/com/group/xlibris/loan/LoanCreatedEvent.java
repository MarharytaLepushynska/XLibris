package com.group.xlibris.loan;

import java.time.Instant;
import java.util.UUID;

public record LoanCreatedEvent(
        UUID loanId,
        UUID bookId,
        UUID ownerId,
        UUID renterId,
        Instant startDate,
        Instant expectedReturnDate
) {
}
