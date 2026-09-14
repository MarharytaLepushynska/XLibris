package com.group.xlibris.report.dto;

import com.group.xlibris.report.enums.ReportType;
import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import jakarta.validation.constraints.*;

import java.net.URI;
import java.util.UUID;

public record ReportRequest(
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 255, groups = {OnCreate.class, OnUpdate.class})
        String title,

        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        ReportType type,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 1000, groups = {OnCreate.class, OnUpdate.class})
        String description,

        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        URI evidenceUrl,

        @NotNull(groups = OnCreate.class)
        UUID reporterId,

        @NotNull(groups = OnCreate.class)
        UUID targetUserId
) {
}
