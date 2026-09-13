package com.group.xlibris.loan.dto;

import com.group.xlibris.loan.enums.LoanStatus;

import java.time.Instant;
import java.util.UUID;

public record LoanResponse (
        UUID id,
        UUID bookId,
        UUID ownerId,
        UUID renterId,
        Instant startDate,
        Instant expectedReturnDate,
        Instant actualReturnDate,
        LoanStatus status
) {
}
