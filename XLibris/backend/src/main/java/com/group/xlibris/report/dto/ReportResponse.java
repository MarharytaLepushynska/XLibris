package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.Report;
import com.group.xlibris.report.ReportAction;
import com.group.xlibris.report.ReportStatus;
import com.group.xlibris.report.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Detailed representation of a report response")
public record ReportResponse(
        @Schema(description = "Unique id of the report", example = "550e8400-e29b-41d4-a716-446655444000")
        UUID id,

        @Schema(description = "Title of the report", example = "Damaged book")
        String title,

        @Schema(description = "Type of the report", example = "DAMAGED_BOOK")
        ReportType type,

        @Schema(description = "Description of the issue", example = "The book cover has a deep scratch")
        String description,

        @Schema(description = "URL to evidence", example = "https://example.com/evidence/photo1.jpg")
        URI evidenceUrl,

        @Schema(description = "Timestamp when the report was created", example = "2026-10-05T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Associated loan id, if its report connected to loan", example = "550e8400-e29b-41d4-a716-446655444001")
        UUID loanId,

        @Schema(description = "Id of the user who submitted the report", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID reporterId,

        @Schema(description = "Id of the target user", example = "550e8400-e29b-41d4-a716-446655447000")
        UUID targetUserId,

        @Schema(description = "Current status of the report", example = "PENDING")
        ReportStatus status,

        @Schema(description = "Moderator comment regarding the resolution", example = "Under review")
        String moderatorComment,

        @Schema(description = "Moderator verdict action", example = "NONE")
        ReportAction moderatorVerdict
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(), report.getTitle(), report.getType(),
                report.getDescription(), report.getEvidenceUrl(), report.getCreatedAt(),
                report.getLoan() != null ? report.getLoan().getId() : null,
                report.getReporter().getId(), report.getTargetUser().getId(),
                report.getStatus(), report.getModeratorComment(), report.getModeratorVerdict());
    }
}
