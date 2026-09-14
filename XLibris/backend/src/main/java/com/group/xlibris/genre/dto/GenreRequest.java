package com.group.xlibris.genre.dto;

import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record GenreRequest(

        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 50, groups = {OnCreate.class, OnUpdate.class})
        String name
) {
}