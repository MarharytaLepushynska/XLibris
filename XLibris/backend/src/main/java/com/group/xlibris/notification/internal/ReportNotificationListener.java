package com.group.xlibris.notification.internal;

import com.group.xlibris.notification.NotificationService;
import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.report.ReportCreatedEvent;
import com.group.xlibris.report.ReportRejectedEvent;
import com.group.xlibris.report.ReportResolvedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportNotificationListener {

    private final NotificationService notificationService;

    @ApplicationModuleListener
    public void handleReportCreated(ReportCreatedEvent event) {

        notificationService.createSystemNotification(
                event.reporterId(),
                NotificationType.REPORT_STATUS_CHANGED,
                "Thank you! Your report \"" + event.title()
                + "\" has been created and is awaiting moderation"
        );
    }

    @ApplicationModuleListener
    public void handleReportResolved(ReportResolvedEvent event) {

        String comment = event.moderatorComment() == null
                         || event.moderatorComment().isBlank()
                ? ""
                : ". Moderator comment: " + event.moderatorComment();

        notificationService.createSystemNotification(
                event.reporterId(),
                NotificationType.REPORT_STATUS_CHANGED,
                "Your report has been resolved. Verdict: "
                + event.verdict()
                + comment
        );

        notificationService.createSystemNotification(
                event.targetUserId(),
                NotificationType.REPORT_STATUS_CHANGED,
                "A report concerning you has been resolved. Verdict: "
                + event.verdict()
                + comment
        );
    }

    @ApplicationModuleListener
    public void handleReportRejected(ReportRejectedEvent event) {

        String reason = event.reason() == null
                        || event.reason().isBlank()
                ? ""
                : ". Reason: " + event.reason();

        notificationService.createSystemNotification(
                event.reporterId(),
                NotificationType.REPORT_STATUS_CHANGED,
                "Your report has been rejected"
                + reason
        );
    }
}