package com.group.xlibris.notification.internal;

import com.group.xlibris.loan.LoanCreatedEvent;
import com.group.xlibris.loan.LoanReturnedEvent;
import com.group.xlibris.notification.NotificationService;
import com.group.xlibris.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanNotificationListener {

    private final NotificationService notificationService;

    @ApplicationModuleListener
    public void handleLoanCreated(LoanCreatedEvent event) {

        notificationService.createSystemNotification(
                event.renterId(),
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan created. Expected return date: "
                + event.expectedReturnDate()
        );

        notificationService.createSystemNotification(
                event.ownerId(),
                NotificationType.LOAN_STATUS_CHANGED,
                "Your book has been loaned. Expected return date: "
                + event.expectedReturnDate()
        );
    }

    @ApplicationModuleListener
    public void handleLoanReturned(LoanReturnedEvent event) {

        notificationService.createSystemNotification(
                event.renterId(),
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan completed. Book returned on: "
                + event.returnDate()
        );

        notificationService.createSystemNotification(
                event.ownerId(),
                NotificationType.LOAN_STATUS_CHANGED,
                "Your book was returned on: "
                + event.returnDate()
        );
    }
}