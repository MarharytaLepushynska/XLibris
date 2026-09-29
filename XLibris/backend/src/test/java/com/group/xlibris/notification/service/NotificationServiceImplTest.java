package com.group.xlibris.notification.service;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;
import com.group.xlibris.notification.internal.Notification;
import com.group.xlibris.notification.internal.NotificationRepository;
import com.group.xlibris.notification.internal.NotificationServiceImpl;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
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

    @Mock
    private UserService userService;

    private NotificationServiceImpl notificationService;

    private UUID notificationId;
    private UUID userId;
    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {

        notificationService =
                new NotificationServiceImpl(
                        notificationRepository,
                        userService
                );

        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = mock(User.class);

        lenient()
                .when(user.getId())
                .thenReturn(userId);

        notification = new Notification(
                notificationId,
                user,
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

        when(userService.getUserReferenceById(userId))
                .thenReturn(user);

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

        verify(userService)
                .getUserReferenceById(userId);

        verify(notificationRepository)
                .save(any(Notification.class));
    }

    @Test
    void shouldCreateSystemNotificationSuccessfully() {

        when(userService.getUserReferenceById(userId))
                .thenReturn(user);

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

        verify(userService)
                .getUserReferenceById(userId);

        verify(notificationRepository)
                .save(any(Notification.class));
    }

    @Test
    void shouldGetNotificationByIdSuccessfully() {

        when(notificationRepository.findByIdWithUser(notificationId))
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
                .findByIdWithUser(notificationId);
    }

    @Test
    void shouldThrowNotFoundWhenNotificationMissing() {

        when(notificationRepository.findByIdWithUser(notificationId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> notificationService.getById(notificationId)
        );

        verify(notificationRepository)
                .findByIdWithUser(notificationId);
    }

    @Test
    void shouldGetAllNotificationsSuccessfully() {

        UUID secondUserId = UUID.randomUUID();

        User secondUser = mock(User.class);

        when(secondUser.getId())
                .thenReturn(secondUserId);

        Notification secondNotification = new Notification(
                UUID.randomUUID(),
                secondUser,
                NotificationType.REPORT_STATUS_CHANGED,
                "Report status changed",
                Instant.now()
        );

        when(notificationRepository.findAllWithUser())
                .thenReturn(
                        List.of(
                                notification,
                                secondNotification
                        )
                );

        List<NotificationResponse> responses =
                notificationService.getAll(null);

        assertEquals(2, responses.size());

        verify(notificationRepository)
                .findAllWithUser();

        verify(
                notificationRepository,
                never()
        ).findByUserIdWithUser(any());
    }

    @Test
    void shouldFilterNotificationsByUserId() {

        when(notificationRepository.findByUserIdWithUser(userId))
                .thenReturn(List.of(notification));

        List<NotificationResponse> responses =
                notificationService.getAll(userId);

        assertEquals(1, responses.size());
        assertEquals(
                userId,
                responses.getFirst().userId()
        );

        verify(notificationRepository)
                .findByUserIdWithUser(userId);

        verify(
                notificationRepository,
                never()
        ).findAllWithUser();
    }
}