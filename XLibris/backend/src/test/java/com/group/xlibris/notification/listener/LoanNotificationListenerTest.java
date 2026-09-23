package com.group.xlibris.notification.listener;

import com.group.xlibris.loan.LoanCreatedEvent;
import com.group.xlibris.loan.LoanReturnedEvent;
import com.group.xlibris.notification.NotificationService;
import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.internal.LoanNotificationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoanNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Test
    void shouldNotifyOwnerAndRenterWhenLoanCreated() {

        UUID ownerId = UUID.randomUUID();
        UUID renterId = UUID.randomUUID();

        Instant expectedReturnDate = Instant.now().plusSeconds(86400);

        LoanCreatedEvent event = new LoanCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ownerId,
                renterId,
                Instant.now(),
                expectedReturnDate
        );

        LoanNotificationListener listener =
                new LoanNotificationListener(notificationService);

        listener.handleLoanCreated(event);

        verify(notificationService).createSystemNotification(
                renterId,
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan created. Expected return date: "
                + expectedReturnDate
        );

        verify(notificationService).createSystemNotification(
                ownerId,
                NotificationType.LOAN_STATUS_CHANGED,
                "Your book has been loaned. Expected return date: "
                + expectedReturnDate
        );
    }

    @Test
    void shouldNotifyOwnerAndRenterWhenLoanReturned() {

        UUID ownerId = UUID.randomUUID();
        UUID renterId = UUID.randomUUID();

        Instant returnDate = Instant.now();

        LoanReturnedEvent event = new LoanReturnedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ownerId,
                renterId,
                returnDate
        );

        LoanNotificationListener listener =
                new LoanNotificationListener(notificationService);

        listener.handleLoanReturned(event);

        verify(notificationService).createSystemNotification(
                renterId,
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan completed. Book returned on: "
                + returnDate
        );

        verify(notificationService).createSystemNotification(
                ownerId,
                NotificationType.LOAN_STATUS_CHANGED,
                "Your book was returned on: "
                + returnDate
        );
    }
}