package com.group.xlibris.notification.internal;

import com.group.xlibris.notification.NotificationType;

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