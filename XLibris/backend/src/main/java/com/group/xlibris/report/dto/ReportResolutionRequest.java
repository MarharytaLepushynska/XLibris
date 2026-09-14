package com.group.xlibris.report.dto;

import com.group.xlibris.report.enums.ReportAction;
import com.group.xlibris.report.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportResolutionRequest(
        @NotNull
        ReportStatus resolution,

        @NotBlank
        @Size(min = 5, max = 1000)
        String moderatorComment,

        @NotNull
        ReportAction moderatorVerdict
) {
}
