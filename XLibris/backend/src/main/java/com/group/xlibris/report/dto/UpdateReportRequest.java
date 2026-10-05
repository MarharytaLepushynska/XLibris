package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.command.UpdateReportCommand;
import com.group.xlibris.report.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;

@Schema(description = "Data to update an existing report")
public record UpdateReportRequest(
        @Schema(description = "Updated title of the report", example = "User havent returned the book")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Updated type of the report", example = "NO_RETURN")
        @NotNull
        ReportType type,

        @Schema(description = "Updated description of the issue[cite: 118]", example = "User have been keeping the book for 4 months")
        @NotBlank
        @Size(max = 1000)
        String description,

        @Schema(description = "Updated URL to evidence", example = "https://example.com/evidence/updated.jpg")
        @NotNull
        URI evidenceUrl
) {
    public UpdateReportCommand toCommand() {
        return new UpdateReportCommand(title, type, description, evidenceUrl);
    }
}