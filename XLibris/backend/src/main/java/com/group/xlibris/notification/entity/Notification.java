package com.group.xlibris.notification.entity;

import com.group.xlibris.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record Notification(
        UUID id,
        UUID userId,
        NotificationType type,
        String message,
        Instant createdAt
) {
}