package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.command.CreateLoanReportCommand;
import com.group.xlibris.report.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;
import java.util.UUID;

@Schema(description = "Data to create a new loan-related report")
public record LoanReportRequest(
        @Schema(description = "Title of the loan report", example = "Damaged book after loan")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Type of the report", example = "DAMAGED_BOOK")
        @NotNull
        ReportType type,

        @Schema(description = "Detailed description of the issue", example = "The book cover has a deep scratch and torn pages")
        @NotBlank
        @Size(max = 1000)
        String description,

        @Schema(description = "URL to evidence such as photos or documents", example = "https://example.com/evidence/photo1.jpg")
        @NotNull
        URI evidenceUrl,

        @Schema(description = "Unique id of the user submitting the report", example = "550e8400-e29b-41d4-a716-446655446000")
        @NotNull
        UUID reporterId
) {
    public CreateLoanReportCommand toCommand() {
        return new CreateLoanReportCommand(title, type, description, evidenceUrl, reporterId);
    }
}
