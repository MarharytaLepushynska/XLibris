package com.group.xlibris.notification.listener;

import com.group.xlibris.loan.LoanReturnedEvent;
import com.group.xlibris.notification.NotificationService;
import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.internal.FeedbackRequestNotificationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FeedbackRequestNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Test
    void shouldRequestFeedbackFromOwnerAndRenterWhenLoanReturned() {

        UUID ownerId = UUID.randomUUID();
        UUID renterId = UUID.randomUUID();

        LoanReturnedEvent event = new LoanReturnedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ownerId,
                renterId,
                Instant.now()
        );

        FeedbackRequestNotificationListener listener =
                new FeedbackRequestNotificationListener(
                        notificationService
                );

        listener.handleLoanReturned(event);

        verify(notificationService).createSystemNotification(
                renterId,
                NotificationType.FEEDBACK_REQUESTED,
                "The loan has been completed. Please leave feedback for the book owner."
        );

        verify(notificationService).createSystemNotification(
                ownerId,
                NotificationType.FEEDBACK_REQUESTED,
                "The loan has been completed. Please leave feedback for the renter."
        );

        verifyNoMoreInteractions(notificationService);
    }
}