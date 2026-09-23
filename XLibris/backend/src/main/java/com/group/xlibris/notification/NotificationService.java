package com.group.xlibris.notification;

import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse create(NotificationRequest request);

    NotificationResponse createSystemNotification(
            UUID userId,
            NotificationType type,
            String message
    );

    NotificationResponse getById(UUID id);

    List<NotificationResponse> getAll(UUID userId);
}