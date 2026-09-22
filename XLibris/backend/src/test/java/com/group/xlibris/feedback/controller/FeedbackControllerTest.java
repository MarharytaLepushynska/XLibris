package com.group.xlibris.feedback.controller;

import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.entity.Feedback;
import com.group.xlibris.feedback.repository.FeedbackRepository;
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
class FeedbackControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeedbackRepository feedbackRepository;

    private UUID feedbackId;
    private UUID loanId;
    private UUID reviewerId;
    private UUID reviewedUserId;

    @BeforeEach
    void resetRepository() {

        feedbackRepository.deleteAll();

        feedbackId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440100");

        UUID feedbackId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440101");

        loanId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440200");

        UUID loanId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440201");

        reviewerId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440300");

        UUID reviewerId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440301");

        reviewedUserId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440400");

        UUID reviewedUserId2 =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440401");

        Feedback feedback1 = new Feedback(
                feedbackId,
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience",
                Instant.now()
        );

        Feedback feedback2 = new Feedback(
                feedbackId2,
                loanId2,
                reviewerId2,
                reviewedUserId2,
                4,
                "Everything was good",
                Instant.now()
        );

        feedbackRepository.save(feedback1);
        feedbackRepository.save(feedback2);
    }

    @Test
    void shouldCreateFeedback() throws Exception {

        FeedbackRequest request = new FeedbackRequest(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440202"),
                UUID.fromString("550e8400-e29b-41d4-a716-446655440302"),
                UUID.fromString("550e8400-e29b-41d4-a716-446655440402"),
                5,
                "Excellent experience"
        );

        mvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Excellent experience"));
    }

    @Test
    void shouldGetFeedbackById() throws Exception {

        mvc.perform(get("/api/feedbacks/{id}", feedbackId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(feedbackId.toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Great experience"));
    }

    @Test
    void shouldGetAllFeedbacks() throws Exception {

        mvc.perform(get("/api/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFilterFeedbacksByReviewedUserId() throws Exception {

        mvc.perform(get("/api/feedbacks")
                        .param(
                                "reviewedUserId",
                                reviewedUserId.toString()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reviewedUserId")
                        .value(reviewedUserId.toString()));
    }

    @Test
    void shouldReturnProblemDetailWithValidationError()
            throws Exception {

        FeedbackRequest request = new FeedbackRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                6,
                ""
        );

        mvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.errors.rating").exists())
                .andExpect(jsonPath("$.errors.comment").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {

        mvc.perform(post("/api/feedbacks")
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

        mvc.perform(get("/api/feedbacks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title")
                        .value("Resource not found"));
    }

    @Test
    void shouldRejectFeedbackForSameUser() throws Exception {

        UUID userId = UUID.randomUUID();

        FeedbackRequest request = new FeedbackRequest(
                UUID.randomUUID(),
                userId,
                userId,
                5,
                "Test"
        );

        mvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Business rule error"));
    }

    @Test
    void shouldRejectDuplicateFeedback() throws Exception {

        FeedbackRequest request = new FeedbackRequest(
                loanId,
                reviewerId,
                reviewedUserId,
                4,
                "Second feedback"
        );

        mvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Business rule violation"));
    }
}