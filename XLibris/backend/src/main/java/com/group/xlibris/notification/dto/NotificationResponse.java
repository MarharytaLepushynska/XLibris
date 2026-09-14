package com.group.xlibris.notification.dto;

import com.group.xlibris.notification.entity.Notification;
import com.group.xlibris.notification.enums.NotificationType;

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
                notification.id(),
                notification.userId(),
                notification.type(),
                notification.message(),
                notification.createdAt()
        );
    }
}