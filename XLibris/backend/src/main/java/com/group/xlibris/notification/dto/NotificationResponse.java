package com.group.xlibris.notification.dto;

import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.internal.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType type,
        String message,
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