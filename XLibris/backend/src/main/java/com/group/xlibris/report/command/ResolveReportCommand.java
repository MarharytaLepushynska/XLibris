package com.group.xlibris.report.command;

import com.group.xlibris.report.enums.ReportAction;
import com.group.xlibris.report.enums.ReportStatus;

public record ResolveReportCommand(
        ReportStatus resolution,
        String moderatorComment,
        ReportAction moderatorVerdict
) {
}
