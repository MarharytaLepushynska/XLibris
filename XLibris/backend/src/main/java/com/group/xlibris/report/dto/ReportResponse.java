package com.group.xlibris.report.dto;

import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.enums.ReportAction;
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
