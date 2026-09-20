package com.group.xlibris.report.command;

import com.group.xlibris.report.enums.ReportType;

import java.net.URI;

public record UpdateReportCommand(
        String title,
        ReportType type,
        String description,
        URI evidenceUrl
) {
}
