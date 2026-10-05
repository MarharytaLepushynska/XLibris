package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.command.CreateReportCommand;
import com.group.xlibris.report.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.net.URI;
import java.util.UUID;

@Schema(description = "Data to create a general user report")
public record ReportRequest(
        @Schema(description = "Title of the report", example = "Inappropriate user behavior")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Type of the report", example = "INAPPROPRIATE_BEHAVIOR")
        @NotNull
        ReportType type,

        @Schema(description = "Detailed description of the issue", example = "User used offensive language in chat messages")
        @NotBlank
        @Size(max = 1000)
        String description,

        @Schema(description = "URL to evidence", example = "https://example.com/evidence/chat-screenshot.png")
        @NotNull
        URI evidenceUrl,

        @Schema(description = "Unique id of the user submitting the report", example = "550e8400-e29b-41d4-a716-446655446000")
        @NotNull
        UUID reporterId,

        @Schema(description = "Unique id of the target user being reported", example = "550e8400-e29b-41d4-a716-446655447000")
        @NotNull
        UUID targetUserId
) {
        public CreateReportCommand toCreateCommand() {
                return new CreateReportCommand(title, type, description, evidenceUrl, reporterId, targetUserId);
        }
}
