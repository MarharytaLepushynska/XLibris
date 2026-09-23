package com.group.xlibris.notification.internal;

import com.group.xlibris.loan.LoanReturnedEvent;
import com.group.xlibris.notification.NotificationService;
import com.group.xlibris.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackRequestNotificationListener {

    private final NotificationService notificationService;

    @ApplicationModuleListener
    public void handleLoanReturned(LoanReturnedEvent event) {

        notificationService.createSystemNotification(
                event.renterId(),
                NotificationType.FEEDBACK_REQUESTED,
                "The loan has been completed. Please leave feedback for the book owner."
        );

        notificationService.createSystemNotification(
                event.ownerId(),
                NotificationType.FEEDBACK_REQUESTED,
                "The loan has been completed. Please leave feedback for the renter."
        );
    }
}