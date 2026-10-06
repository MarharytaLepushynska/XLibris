package com.group.xlibris.validator;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "xlibris.validator")
@Validated
public record ValidatorProperties(
        boolean enabled,

        @NotBlank(message = "Validation title cannot be blank")
        String title,

        @NotBlank(message = "Validation detail cannot be blank")
        String detail
) {
    public ValidatorProperties {
        if (title == null || title.isBlank()) {
            title = "Validation failed";
        }

        if (detail == null || detail.isBlank()) {
            detail = "Request contains invalid fields";
        }
    }
}