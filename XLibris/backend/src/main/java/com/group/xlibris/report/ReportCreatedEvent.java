package com.group.xlibris.report;

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
