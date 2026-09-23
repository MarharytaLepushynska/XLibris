package com.group.xlibris.loan.dto;

import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.loan.LoanStatus;
import org.springframework.modulith.NamedInterface;

import java.time.Instant;
import java.util.UUID;

@NamedInterface(value = "api")
public record LoanResponse(
        UUID id,
        UUID bookId,
        UUID ownerId,
        UUID renterId,
        Instant startDate,
        Instant expectedReturnDate,
        Instant actualReturnDate,
        LoanStatus status
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBookId(),
                loan.getOwnerId(),
                loan.getRenterId(),
                loan.getStartDate(),
                loan.getExpectedReturnDate(),
                loan.getActualReturnDate(),
                loan.getStatus()
        );
    }
}
