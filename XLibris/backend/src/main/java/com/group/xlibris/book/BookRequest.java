package com.group.xlibris.book;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BookRequest(

        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 100, groups = {OnCreate.class, OnUpdate.class})
        String title,

        @Size(max = 2000, groups = {OnCreate.class, OnUpdate.class})
        String description,

        String photoURL,

        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        UUID ownerId,

        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        UUID authorId,

        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        UUID genreId

) {
}
