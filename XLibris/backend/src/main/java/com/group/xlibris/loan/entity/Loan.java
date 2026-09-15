package com.group.xlibris.loan.entity;

import com.group.xlibris.loan.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class Loan {
    private UUID id;
    private UUID bookId;
    private UUID ownerId;
    private UUID renterId;
    private Instant startDate;
    private Instant expectedReturnDate;
    private Instant actualReturnDate;
    private LoanStatus status;

    public void assignToReturned() {
        if (this.status != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Only loans in ACTIVE status can be returned");
        }
        actualReturnDate = Instant.now();
        this.status = LoanStatus.RETURNED;
    }
}
