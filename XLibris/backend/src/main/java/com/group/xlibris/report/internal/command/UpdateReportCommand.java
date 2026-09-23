package com.group.xlibris.report.internal.command;

import com.group.xlibris.report.ReportType;

import java.net.URI;

public record UpdateReportCommand(
        String title,
        ReportType type,
        String description,
        URI evidenceUrl
) {
}
