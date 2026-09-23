package com.group.xlibris;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AuthorRequest(

        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 255, groups = {OnCreate.class, OnUpdate.class})
        String name
) {
}