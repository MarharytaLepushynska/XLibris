package com.group.xlibris.user.dto;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import com.group.xlibris.user.internal.CreateUserCommand;
import com.group.xlibris.user.internal.UpdateUserCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Data to create or update a user profile")
public record UserRequest(
        @Schema(description = "Unique identifier of the user (must be null on create, required on update)", example = "550e8400-e29b-41d4-a716-446655446000")
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @Schema(description = "Full name of the user", example = "John Doe")
        @NotBlank(groups = { OnCreate.class, OnUpdate.class })
        @Size(max = 255, groups = { OnCreate.class, OnUpdate.class })
        String name,

        @Schema(description = "City of residence", example = "Kyiv")
        @NotBlank(groups = { OnCreate.class, OnUpdate.class })
        @Size(max = 70, groups = { OnCreate.class, OnUpdate.class })
        String city,

        @Schema(description = "URL to the user's profile photo", example = "https://example.com/photos/john.jpg")
        String photoURL,

        @Schema(description = "Email address of the user", example = "john.doe@example.com")
        @NotBlank(groups = { OnCreate.class, OnUpdate.class })
        @Size(max = 255, groups = { OnCreate.class, OnUpdate.class })
        @Email(groups = { OnCreate.class, OnUpdate.class })
        String email,

        @Schema(description = "Phone number in Ukrainian format", example = "+380501234567")
        @Pattern(regexp = "^\\+380\\d{9}$", groups = { OnCreate.class, OnUpdate.class })
        String phone
) {
        public CreateUserCommand toCommand() {
                return new CreateUserCommand(
                        this.name,
                        this.city,
                        this.photoURL,
                        this.email,
                        this.phone
                );
        }

        public UpdateUserCommand toUpdateCommand() {
                return new UpdateUserCommand(
                        this.id,
                        this.name,
                        this.city,
                        this.photoURL,
                        this.email,
                        this.phone
                );
        }
}
