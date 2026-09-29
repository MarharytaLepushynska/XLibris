package com.group.xlibris.notification.internal;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.notification.NotificationService;
import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserService userService
    ) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
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

        User user =
                userService.getUserReferenceById(userId);

        Notification notification = new Notification(
                UUID.randomUUID(),
                user,
                type,
                message,
                Instant.now()
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        System.out.println(
                "Notification with id "
                + savedNotification.getId()
                + " was created"
        );

        return NotificationResponse.from(savedNotification);
    }

    @Override
    public NotificationResponse getById(UUID id) {

        Notification notification =
                notificationRepository.findByIdWithUser(id)
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

        List<Notification> notifications;

        if (userId == null) {
            notifications =
                    notificationRepository.findAllWithUser();
        } else {
            notifications =
                    notificationRepository.findByUserIdWithUser(userId);
        }

        return notifications
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }
}