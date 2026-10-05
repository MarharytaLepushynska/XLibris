package com.group.xlibris.report.dto;

import com.group.xlibris.report.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Criteria used to filter reports")
public record ReportFilterCriteria(
        @Schema(description = "Filter by report status", example = "IN_REVIEW")
        ReportStatus status,

        @Schema(description = "Filter by associated loan id", example = "550e8400-e29b-41d4-a716-446655444000")
        UUID loanId,
        @Schema(description = "Filter by reporter user id", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID reporterId,
        @Schema(description = "Filter by target user id", example = "550e8400-e29b-41d4-a716-446655447000")
        UUID targetUserId
) {
}
