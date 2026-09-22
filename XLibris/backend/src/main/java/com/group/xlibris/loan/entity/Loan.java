package com.group.xlibris.loan.entity;

import com.group.xlibris.loan.exception.InvalidLoanStateException;
import com.group.xlibris.loan.enums.LoanStatus;
import com.group.xlibris.loan.exception.InvalidReturnDateException;
import com.group.xlibris.loan.exception.SameParticipantException;
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

    public static Loan create(UUID bookId, UUID ownerId, UUID renterId, Instant expectedReturnDate) {
        if (ownerId.equals(renterId)) {
            throw new SameParticipantException("Owner cannot rent their own book");
        }
        Instant now = Instant.now();
        if (expectedReturnDate == null || !expectedReturnDate.isAfter(now)) {
            throw new InvalidReturnDateException("Expected return date must be in the future");
        }
        return new Loan(UUID.randomUUID(), bookId, ownerId, renterId, now, expectedReturnDate, null);
    }

    public LoanStatus getStatus() {
        return getStatus(Instant.now());
    }

    public LoanStatus getStatus(Instant currentInstant) {
        if (actualReturnDate != null) {
            return LoanStatus.RETURNED;
        } else if (currentInstant.isAfter(expectedReturnDate)) {
            return LoanStatus.OVERDUE;
        } else {
            return LoanStatus.ACTIVE;
        }
    }

    public void assignToReturned() {
        if (actualReturnDate != null) {
            throw new InvalidLoanStateException("Only not returned loans can be returned");
        }
        actualReturnDate = Instant.now();
    }
}
