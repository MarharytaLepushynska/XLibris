package com.group.xlibris.loan.internal;

import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.bookRequest.BookRequestStatusChangedEvent;
import com.group.xlibris.loan.LoanService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class BookRequestEventListener {

    private final LoanService loanService;

    @EventListener
    public void onBookRequestStatusChanged(BookRequestStatusChangedEvent event) {
        if (event.newStatus() != BookRequestStatus.FULFILLED) {
            return;
        }

        int durationDays = event.desiredDurationDays() > 0 ? event.desiredDurationDays() : 14;
        Instant expectedReturnDate = Instant.now().plus(durationDays, ChronoUnit.DAYS);

        CreateLoanCommand command = new CreateLoanCommand(
                event.bookId(),
                event.ownerId(),
                event.requesterId(),
                expectedReturnDate
        );

        loanService.createLoan(command);
    }
}