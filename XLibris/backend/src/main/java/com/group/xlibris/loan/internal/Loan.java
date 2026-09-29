package com.group.xlibris.loan.internal;

import com.group.xlibris.book.Book;
import com.group.xlibris.loan.InvalidLoanStateException;
import com.group.xlibris.loan.LoanStatus;
import com.group.xlibris.loan.InvalidReturnDateException;
import com.group.xlibris.loan.SameParticipantException;
import com.group.xlibris.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Entity
@Table(name = "loans")
public class Loan {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    @Column(nullable = false, updatable = false)
    private Instant startDate;

    @Column(nullable = false, updatable = false)
    private Instant expectedReturnDate;

    private Instant actualReturnDate;

    protected Loan() {}

    public static Loan create(Book book, User owner, User renter, Instant expectedReturnDate) {
        if (owner.getId().equals(renter.getId())) {
            throw new SameParticipantException("Owner cannot rent their own book");
        }
        Instant now = Instant.now();
        if (expectedReturnDate == null || !expectedReturnDate.isAfter(now)) {
            throw new InvalidReturnDateException("Expected return date must be in the future");
        }
        return new Loan(UUID.randomUUID(), book, owner, renter, now, expectedReturnDate, null);
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
