package com.group.xlibris.user.dto;

import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record UserRequest(
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @NotBlank(groups = { OnCreate.class, OnUpdate.class })
        @Size(max = 255, groups = { OnCreate.class, OnUpdate.class })
        String name,

        @NotBlank(groups = { OnCreate.class, OnUpdate.class })
        @Size(max = 70, groups = { OnCreate.class, OnUpdate.class })
        String city,

        String photoURL,

        @NotBlank(groups = { OnCreate.class, OnUpdate.class })
        @Size(max = 255, groups = { OnCreate.class, OnUpdate.class })
        @Email(groups = { OnCreate.class, OnUpdate.class })
        String email,

        @Pattern(regexp = "^\\+380\\d{9}$", groups = { OnCreate.class, OnUpdate.class })
        String phone
) {
}
