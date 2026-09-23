package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.command.CreateLoanReportCommand;
import com.group.xlibris.report.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;
import java.util.UUID;

public record LoanReportRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotNull
        ReportType type,

        @NotBlank
        @Size(max = 1000)
        String description,

        @NotNull
        URI evidenceUrl,

        @NotNull
        UUID reporterId
) {
    public CreateLoanReportCommand toCommand() {
        return new CreateLoanReportCommand(title, type, description, evidenceUrl, reporterId);
    }
}
