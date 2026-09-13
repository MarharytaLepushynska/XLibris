package com.group.xlibris.report.dto;

import com.group.xlibris.report.enums.ReportStatus;
import com.group.xlibris.report.enums.ReportType;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        String title,
        ReportType type,
        String description,
        URI evidenceUrl,
        Instant createdAt,
        UUID reporterId,
        UUID targetUserId,
        ReportStatus status
) {
}
