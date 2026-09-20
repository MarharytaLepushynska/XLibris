package com.group.xlibris.report.dto;

import com.group.xlibris.report.enums.ReportStatus;

import java.util.UUID;

public record ReportFilterCriteria(
        ReportStatus status,
        UUID loanId,
        UUID reporterId,
        UUID targetUserId
) {
}
