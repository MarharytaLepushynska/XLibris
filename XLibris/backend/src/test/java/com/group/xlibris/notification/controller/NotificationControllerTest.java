package com.group.xlibris.notification.controller;

import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.entity.Notification;
import com.group.xlibris.notification.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationController notificationController;

    private UUID notificationId;
    private UUID userId;

    @BeforeEach
    void resetMap() {

        notificationController.clearMap();

        notificationId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440500");

        UUID notificationId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440501");

        userId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440600");

        UUID userId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440601");

        Notification notification1 = new Notification(
                notificationId,
                userId,
                NotificationType.LOAN_STATUS_CHANGED,
                "Loan status changed",
                Instant.now()
        );

        Notification notification2 = new Notification(
                notificationId2,
                userId2,
                NotificationType.REPORT_STATUS_CHANGED,
                "Report status changed",
                Instant.now()
        );

        notificationController.fillMap(notification1);
        notificationController.fillMap(notification2);
    }

    @Test
    void shouldCreateNotification() throws Exception {

        UUID newUserId = UUID.randomUUID();

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
}