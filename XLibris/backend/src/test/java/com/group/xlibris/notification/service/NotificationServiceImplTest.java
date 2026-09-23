package com.group.xlibris.notification.service;

import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;
import com.group.xlibris.notification.entity.Notification;
import com.group.xlibris.notification.enums.NotificationType;
import com.group.xlibris.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl notificationService;

    private UUID notificationId;
    private UUID userId;
    private Notification notification;

    @BeforeEach
    void setUp() {

        notificationService =
                new NotificationServiceImpl(notificationRepository);

        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        notification = new Notification(
                notificationId,
                userId,
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan status changed",
                Instant.now()
        );
    }

    @Test
    void shouldCreateNotificationSuccessfully() {

        NotificationRequest request = new NotificationRequest(
                userId,
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan status changed"
        );

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response =
                notificationService.create(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(userId, response.userId());
        assertEquals(
                NotificationType.LOAN_STATUS_CHANGED,
                response.type()
        );
        assertEquals(
                "Loan status changed",
                response.message()
        );
        assertNotNull(response.createdAt());

        verify(notificationRepository)
                .save(any(Notification.class));
    }

    @Test
    void shouldCreateSystemNotificationSuccessfully() {

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response =
                notificationService.createSystemNotification(
                        userId,
                        NotificationType.LOAN_DEADLINE_APPROACHING,
                        "Loan deadline is approaching"
                );

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(userId, response.userId());
        assertEquals(
                NotificationType.LOAN_DEADLINE_APPROACHING,
                response.type()
        );
        assertEquals(
                "Loan deadline is approaching",
                response.message()
        );
        assertNotNull(response.createdAt());

        verify(notificationRepository)
                .save(any(Notification.class));
    }

    @Test
    void shouldGetNotificationByIdSuccessfully() {

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        NotificationResponse response =
                notificationService.getById(notificationId);

        assertNotNull(response);
        assertEquals(notificationId, response.id());
        assertEquals(userId, response.userId());
        assertEquals(
                NotificationType.LOAN_STATUS_CHANGED,
                response.type()
        );
        assertEquals(
                "Loan status changed",
                response.message()
        );

        verify(notificationRepository)
                .findById(notificationId);
    }

    @Test
    void shouldThrowNotFoundWhenNotificationMissing() {

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> notificationService.getById(notificationId)
        );

        verify(notificationRepository)
                .findById(notificationId);
    }

    @Test
    void shouldGetAllNotificationsSuccessfully() {

        Notification secondNotification = new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationType.REPORT_STATUS_CHANGED,
                "Report status changed",
                Instant.now()
        );

        when(notificationRepository.findAll())
                .thenReturn(
                        List.of(
                                notification,
                                secondNotification
                        )
                );

        List<NotificationResponse> responses =
                notificationService.getAll(null);

        assertEquals(2, responses.size());

        verify(notificationRepository).findAll();
    }

    @Test
    void shouldFilterNotificationsByUserId() {

        UUID secondUserId = UUID.randomUUID();

        Notification secondNotification = new Notification(
                UUID.randomUUID(),
                secondUserId,
                NotificationType.REPORT_STATUS_CHANGED,
                "Report status changed",
                Instant.now()
        );

        when(notificationRepository.findAll())
                .thenReturn(
                        List.of(
                                notification,
                                secondNotification
                        )
                );

        List<NotificationResponse> responses =
                notificationService.getAll(userId);

        assertEquals(1, responses.size());
        assertEquals(
                userId,
                responses.getFirst().userId()
        );

        verify(notificationRepository).findAll();
    }
}