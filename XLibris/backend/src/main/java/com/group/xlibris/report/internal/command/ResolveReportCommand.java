package com.group.xlibris.report.internal.command;

import com.group.xlibris.report.ReportAction;
import com.group.xlibris.report.ReportStatus;

public record ResolveReportCommand(
        ReportStatus resolution,
        String moderatorComment,
        ReportAction moderatorVerdict
) {
}
