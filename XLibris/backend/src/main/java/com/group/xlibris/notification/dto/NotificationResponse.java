package com.group.xlibris.notification.dto;

import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.internal.Notification;
import io.swagger.v3.oas.annotations.media.Schema;


import java.time.Instant;
import java.util.UUID;

@Schema(description = "Detailed representation of a notification response")
public record NotificationResponse(
        @Schema(description = "Unique id of the notification", example = "550e8400-e29b-41d4-a716-446655444000")
        UUID id,

        @Schema(description = "Unique id of the recipient user", example = "550e8400-e29b-41d4-a716-446655447000")
        UUID userId,

        @Schema(description = "Type of the notification", example = "BOOK_REQUEST_CREATED")
        NotificationType type,

        @Schema(description = "Text message content of the notification", example = "Your book loan request has been sent.")
        String message,

        @Schema(description = "Timestamp when the notification was created", example = "2026-10-05T10:00:00Z")
        Instant createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}