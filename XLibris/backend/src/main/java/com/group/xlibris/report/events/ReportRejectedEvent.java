package com.group.xlibris.report.events;

import java.time.Instant;
import java.util.UUID;

public record ReportRejectedEvent(
        UUID reportId,
        UUID reporterId,
        UUID targetUserId,
        String reason,
        Instant rejectedAt
) {
}
