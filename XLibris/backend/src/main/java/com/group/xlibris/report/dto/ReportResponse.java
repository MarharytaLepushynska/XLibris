package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.Report;
import com.group.xlibris.report.ReportAction;
import com.group.xlibris.report.ReportStatus;
import com.group.xlibris.report.ReportType;

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
        UUID loanId,
        UUID reporterId,
        UUID targetUserId,
        ReportStatus status,
        String moderatorComment,
        ReportAction moderatorVerdict
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(), report.getTitle(), report.getType(),
                report.getDescription(), report.getEvidenceUrl(), report.getCreatedAt(),
                report.getLoanId(), report.getReporterId(), report.getTargetUserId(),
                report.getStatus(), report.getModeratorComment(), report.getModeratorVerdict());
    }
}
