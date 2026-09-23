package com.group.xlibris.report.internal.command;

import com.group.xlibris.report.ReportType;

import java.net.URI;
import java.util.UUID;

public record CreateReportCommand(
        String title,
        ReportType type,
        String description,
        URI evidenceUrl,
        UUID reporterId,
        UUID targetUserId
) {
}
