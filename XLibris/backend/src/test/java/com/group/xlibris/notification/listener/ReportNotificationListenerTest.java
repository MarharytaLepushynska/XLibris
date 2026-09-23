package com.group.xlibris.notification.listener;

import com.group.xlibris.notification.NotificationService;
import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.internal.ReportNotificationListener;
import com.group.xlibris.report.ReportAction;
import com.group.xlibris.report.ReportCreatedEvent;
import com.group.xlibris.report.ReportRejectedEvent;
import com.group.xlibris.report.ReportResolvedEvent;
import com.group.xlibris.report.ReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Test
    void shouldNotifyOnlyReporterWhenReportCreated() {

        UUID reporterId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        ReportCreatedEvent event = new ReportCreatedEvent(
                UUID.randomUUID(),
                reporterId,
                targetUserId,
                UUID.randomUUID(),
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Inappropriate behaviour",
                Instant.now()
        );

        ReportNotificationListener listener =
                new ReportNotificationListener(notificationService);

        listener.handleReportCreated(event);

        verify(notificationService).createSystemNotification(
                reporterId,
                NotificationType.REPORT_STATUS_CHANGED,
                "Thank you! Your report \"Inappropriate behaviour\" has been created and is awaiting moderation"
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotifyReporterAndTargetUserWhenReportResolved() {

        UUID reporterId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        ReportResolvedEvent event = new ReportResolvedEvent(
                UUID.randomUUID(),
                reporterId,
                targetUserId,
                ReportAction.WARNING,
                "User received a warning",
                Instant.now()
        );

        ReportNotificationListener listener =
                new ReportNotificationListener(notificationService);

        listener.handleReportResolved(event);

        verify(notificationService).createSystemNotification(
                reporterId,
                NotificationType.REPORT_STATUS_CHANGED,
                "Your report has been resolved. Verdict: WARNING. Moderator comment: User received a warning"
        );

        verify(notificationService).createSystemNotification(
                targetUserId,
                NotificationType.REPORT_STATUS_CHANGED,
                "A report concerning you has been resolved. Verdict: WARNING. Moderator comment: User received a warning"
        );
    }

    @Test
    void shouldNotifyOnlyReporterWhenReportRejected() {

        UUID reporterId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        ReportRejectedEvent event = new ReportRejectedEvent(
                UUID.randomUUID(),
                reporterId,
                targetUserId,
                "Insufficient evidence",
                Instant.now()
        );

        ReportNotificationListener listener =
                new ReportNotificationListener(notificationService);

        listener.handleReportRejected(event);

        verify(notificationService).createSystemNotification(
                reporterId,
                NotificationType.REPORT_STATUS_CHANGED,
                "Your report has been rejected. Reason: Insufficient evidence"
        );

        verifyNoMoreInteractions(notificationService);
    }
}