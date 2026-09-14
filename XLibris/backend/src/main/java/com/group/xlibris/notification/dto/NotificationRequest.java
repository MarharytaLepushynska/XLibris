package com.group.xlibris.notification.dto;

import com.group.xlibris.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationRequest(

        @NotNull
        UUID userId,

        @NotNull
        NotificationType type,

        @NotBlank
        String message

) {
}