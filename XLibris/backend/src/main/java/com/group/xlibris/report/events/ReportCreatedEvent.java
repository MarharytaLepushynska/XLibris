package com.group.xlibris.report.events;

import com.group.xlibris.report.enums.ReportType;

import java.time.Instant;
import java.util.UUID;

public record ReportCreatedEvent(
        UUID reportId,
        UUID reporterId,
        UUID targetUserId,
        UUID loanId,
        ReportType type,
        String title,
        Instant createdAt
) {
}
