package com.group.xlibris.report.command;

import com.group.xlibris.report.enums.ReportType;

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
