package com.group.xlibris.landCommon;

import java.util.UUID;

public record LoanReturnedEvent(
        UUID loanId,
        UUID bookId
) {
}
