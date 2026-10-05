package com.group.xlibris.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contact information of a user, accessible only with a confirmed loan")
public record UserContactInfo(
        @Schema(description = "Email address of the user", example = "john.doe@example.com")
        String email,

        @Schema(description = "Phone number in Ukrainian format", example = "+380501234567")
        String phone
) {
}
