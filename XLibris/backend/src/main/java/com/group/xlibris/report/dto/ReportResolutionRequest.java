package com.group.xlibris.report.dto;

import com.group.xlibris.report.internal.command.ResolveReportCommand;
import com.group.xlibris.report.ReportAction;
import com.group.xlibris.report.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for moderating and resolving a report")
public record ReportResolutionRequest(
        @Schema(description = "Resolved status of the report", example = "RESOLVED")
        @NotNull
        ReportStatus resolution,

        @Schema(description = "Comment provided by the moderator explaining the decision", example = "Warning issued to the user based on provided evidence")
        @NotBlank
        @Size(min = 5, max = 1000)
        String moderatorComment,

        @Schema(description = "Action taken by the moderator@Schema(description = \"Moderator verdict action[cite: 117]\", example = \"NONE[cite: 117]\")", example = "WARN")
        @NotNull
        ReportAction moderatorVerdict
) {
    public ResolveReportCommand toCommand() {
        return new ResolveReportCommand(
                this.resolution(),
                this.moderatorComment(),
                this.moderatorVerdict());
    }
}
