package com.group.xlibris.loan.listener;

import com.group.xlibris.bookRequest.BookRequestStatusChangedEvent;
import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.internal.BookRequestEventListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookRequestEventListenerTest {

    @Mock
    private LoanService loanService;

    @Test
    void shouldCreateLoanWhenBookRequestFulfilled() {

        UUID bookId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        BookRequestStatusChangedEvent event = new BookRequestStatusChangedEvent(
                UUID.randomUUID(),
                bookId,
                requesterId,
                ownerId,
                14,
                BookRequestStatus.APPROVED,
                BookRequestStatus.FULFILLED
        );

        BookRequestEventListener listener =
                new BookRequestEventListener(loanService);

        listener.onBookRequestStatusChanged(event);

        verify(loanService).createLoan(argThat(command ->
                command.bookId().equals(bookId)
                        && command.ownerId().equals(ownerId)
                        && command.renterId().equals(requesterId)
                        && command.expectedReturnDate() != null
        ));

        verifyNoMoreInteractions(loanService);
    }

    @Test
    void shouldNotCreateLoanWhenBookRequestNotFulfilled() {

        BookRequestStatusChangedEvent event = new BookRequestStatusChangedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                14,
                BookRequestStatus.PENDING,
                BookRequestStatus.APPROVED
        );

        BookRequestEventListener listener =
                new BookRequestEventListener(loanService);

        listener.onBookRequestStatusChanged(event);

        verifyNoInteractions(loanService);
    }
}