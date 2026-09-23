package com.group.xlibris.loan;

import java.util.UUID;

public record LoanReturnedEvent(
        UUID loanId,
        UUID bookId
) {
}
