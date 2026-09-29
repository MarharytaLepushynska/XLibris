package com.group.xlibris.notification.controller;

import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.internal.Notification;
import com.group.xlibris.notification.internal.NotificationRepository;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.internal.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID notificationId;
    private UUID userId;

    @BeforeEach
    void resetRepository() {

        notificationRepository.deleteAll();
        userRepository.deleteAll();

        notificationId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440500");

        UUID notificationId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440501");

        userId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440600");

        UUID userId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440601");

        User user1 = createUser(
                userId,
                "User One",
                "user1@example.com"
        );

        User user2 = createUser(
                userId2,
                "User Two",
                "user2@example.com"
        );

        userRepository.save(user1);
        userRepository.save(user2);

        Notification notification1 = new Notification(
                notificationId,
                user1,
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan status changed",
                Instant.now()
        );

        Notification notification2 = new Notification(
                notificationId2,
                user2,
                NotificationType.REPORT_STATUS_CHANGED,
                "Report status changed",
                Instant.now()
        );

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
    }

    @Test
    void shouldCreateNotification() throws Exception {

        UUID newUserId = UUID.randomUUID();

        User newUser = createUser(
                newUserId,
                "New User",
                "new.user@example.com"
        );

        userRepository.save(newUser);

        NotificationRequest request = new NotificationRequest(
                newUserId,
                NotificationType.LOAN_DEADLINE_APPROACHING,
                "The loan deadline is approaching"
        );

        mvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId")
                        .value(newUserId.toString()))
                .andExpect(jsonPath("$.type")
                        .value("LOAN_DEADLINE_APPROACHING"))
                .andExpect(jsonPath("$.message")
                        .value("The loan deadline is approaching"));
    }

    @Test
    void shouldGetNotificationById() throws Exception {

        mvc.perform(
                        get(
                                "/api/notifications/{id}",
                                notificationId
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(notificationId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.type")
                        .value("LOAN_STATUS_CHANGED"))
                .andExpect(jsonPath("$.message")
                        .value("Loan status changed"));
    }

    @Test
    void shouldGetAllNotifications() throws Exception {

        mvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFilterNotificationsByUserId() throws Exception {

        mvc.perform(get("/api/notifications")
                        .param(
                                "userId",
                                userId.toString()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId")
                        .value(userId.toString()));
    }

    @Test
    void shouldReturnProblemDetailWithValidationError()
            throws Exception {

        NotificationRequest request =
                new NotificationRequest(
                        null,
                        NotificationType.LOAN_STATUS_CHANGED,
                        ""
                );

        mvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.errors.userId").exists())
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {

        mvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid request body"));
    }

    @Test
    void shouldReturnNotFoundProblemDetail() throws Exception {

        UUID id =
                UUID.fromString(
                        "550e8400-e29b-41d4-a716-446655440999"
                );

        mvc.perform(
                        get(
                                "/api/notifications/{id}",
                                id
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title")
                        .value("Resource not found"));
    }

    @Test
    void shouldRejectUnknownJsonField() throws Exception {

        String requestBody = """
                {
                  "userId": "550e8400-e29b-41d4-a716-446655440600",
                  "type": "LOAN_STATUS_CHANGED",
                  "message": "Loan status changed",
                  "unknownField": "unexpected"
                }
                """;

        mvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid request body"));
    }

    private User createUser(
            UUID id,
            String name,
            String email
    ) {

        return new User(
                id,
                name,
                "Kyiv",
                null,
                email,
                "+380501234567",
                Instant.now(),
                Role.USER,
                0.0,
                0.0,
                0,
                0,
                0
        );
    }
}