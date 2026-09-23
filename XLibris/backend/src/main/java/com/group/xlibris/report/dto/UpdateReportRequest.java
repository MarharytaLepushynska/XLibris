package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.command.UpdateReportCommand;
import com.group.xlibris.report.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;

public record UpdateReportRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotNull
        ReportType type,

        @NotBlank
        @Size(max = 1000)
        String description,

        @NotNull
        URI evidenceUrl
) {
    public UpdateReportCommand toCommand() {
        return new UpdateReportCommand(title, type, description, evidenceUrl);
    }
}