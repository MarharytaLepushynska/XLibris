package com.group.xlibris.notification.dto;

import com.group.xlibris.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Data to create a new notification")
public record NotificationRequest(

        @Schema(description = "Unique id of the user to receive the notification", example = "550e8400-e29b-41d4-a716-446655447000")
        @NotNull
        UUID userId,

        @Schema(description = "Type of the notification", example = "BOOK_REQUEST_CREATED")
        @NotNull
        NotificationType type,

        @Schema(description = "Text message content of the notification", example = "Your book loan request has been sent.")
        @NotBlank
        String message

) {
}