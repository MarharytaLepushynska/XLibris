package com.group.xlibris.notification.service;

import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;
import com.group.xlibris.notification.entity.Notification;
import com.group.xlibris.notification.enums.NotificationType;
import com.group.xlibris.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationResponse create(NotificationRequest request) {

        return createSystemNotification(
                request.userId(),
                request.type(),
                request.message()
        );
    }

    @Override
    public NotificationResponse createSystemNotification(
            UUID userId,
            NotificationType type,
            String message
    ) {

        Notification notification = new Notification(
                UUID.randomUUID(),
                userId,
                type,
                message,
                Instant.now()
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        return NotificationResponse.from(savedNotification);
    }

    @Override
    public NotificationResponse getById(UUID id) {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Notification with id "
                                        + id
                                        + " not found"
                                )
                        );

        return NotificationResponse.from(notification);
    }

    @Override
    public List<NotificationResponse> getAll(UUID userId) {

        return notificationRepository.findAll()
                .stream()
                .filter(notification ->
                        userId == null
                        || notification.userId().equals(userId)
                )
                .map(NotificationResponse::from)
                .toList();
    }
}