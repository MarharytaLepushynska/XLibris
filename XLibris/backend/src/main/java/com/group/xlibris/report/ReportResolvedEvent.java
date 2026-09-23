package com.group.xlibris.report;

import java.time.Instant;
import java.util.UUID;

public record ReportResolvedEvent(
        UUID reportId,
        UUID reporterId,
        UUID targetUserId,
        ReportAction verdict,
        String moderatorComment,
        Instant resolvedAt
) {
}
