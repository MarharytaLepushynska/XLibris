package com.group.xlibris.feedback.controller;

import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.internal.Feedback;
import com.group.xlibris.feedback.internal.FeedbackRepository;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.loan.internal.LoanRepository;
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
class FeedbackControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

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

        User reviewer = createUser(
                reviewerId,
                "Reviewer One",
                "reviewer1@example.com"
        );

        User reviewedUser = createUser(
                reviewedUserId,
                "Reviewed User One",
                "reviewed1@example.com"
        );

        User reviewer2 = createUser(
                reviewerId2,
                "Reviewer Two",
                "reviewer2@example.com"
        );

        User reviewedUser2 = createUser(
                reviewedUserId2,
                "Reviewed User Two",
                "reviewed2@example.com"
        );

        userRepository.save(reviewer);
        userRepository.save(reviewedUser);
        userRepository.save(reviewer2);
        userRepository.save(reviewedUser2);

        Book book1 = createBook(
                UUID.randomUUID(),
                reviewedUser,
                "Book One"
        );

        Book book2 = createBook(
                UUID.randomUUID(),
                reviewedUser2,
                "Book Two"
        );

        bookRepository.save(book1);
        bookRepository.save(book2);

        Loan returnedLoan1 = new Loan(
                loanId,
                book1,
                reviewedUser,
                reviewer,
                Instant.now().minusSeconds(172800),
                Instant.now().minusSeconds(86400),
                Instant.now().minusSeconds(3600)
        );

        Loan returnedLoan2 = new Loan(
                loanId2,
                book2,
                reviewedUser2,
                reviewer2,
                Instant.now().minusSeconds(172800),
                Instant.now().minusSeconds(86400),
                Instant.now().minusSeconds(3600)
        );

        loanRepository.save(returnedLoan1);
        loanRepository.save(returnedLoan2);

        Feedback feedback1 = new Feedback(
                feedbackId,
                returnedLoan1,
                reviewer,
                reviewedUser,
                5,
                "Great experience",
                Instant.now()
        );

        Feedback feedback2 = new Feedback(
                feedbackId2,
                returnedLoan2,
                reviewer2,
                reviewedUser2,
                4,
                "Everything was good",
                Instant.now()
        );

        feedbackRepository.save(feedback1);
        feedbackRepository.save(feedback2);
    }

    @Test
    void shouldCreateFeedback() throws Exception {

        UUID newLoanId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440202");

        UUID newReviewerId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440302");

        UUID newReviewedUserId =
                UUID.fromString("550e8400-e29b-41d4-a716-446655440402");

        User reviewer = createUser(
                newReviewerId,
                "New Reviewer",
                "new.reviewer@example.com"
        );

        User reviewedUser = createUser(
                newReviewedUserId,
                "New Reviewed User",
                "new.reviewed@example.com"
        );

        userRepository.save(reviewer);
        userRepository.save(reviewedUser);

        Book book = createBook(
                UUID.randomUUID(),
                reviewedUser,
                "New Book"
        );

        bookRepository.save(book);

        Loan returnedLoan = new Loan(
                newLoanId,
                book,
                reviewedUser,
                reviewer,
                Instant.now().minusSeconds(172800),
                Instant.now().minusSeconds(86400),
                Instant.now().minusSeconds(3600)
        );

        loanRepository.save(returnedLoan);

        FeedbackRequest request = new FeedbackRequest(
                newLoanId,
                newReviewerId,
                newReviewedUserId,
                5,
                "Excellent experience"
        );

        mvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.loanId")
                        .value(newLoanId.toString()))
                .andExpect(jsonPath("$.reviewerId")
                        .value(newReviewerId.toString()))
                .andExpect(jsonPath("$.reviewedUserId")
                        .value(newReviewedUserId.toString()))
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
                .andExpect(jsonPath("$.loanId")
                        .value(loanId.toString()))
                .andExpect(jsonPath("$.reviewerId")
                        .value(reviewerId.toString()))
                .andExpect(jsonPath("$.reviewedUserId")
                        .value(reviewedUserId.toString()))
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

    private Book createBook(
            UUID id,
            User owner,
            String title
    ) {

        return new Book(
                id,
                title,
                "Test description",
                null,
                BookStatus.AVAILABLE,
                owner,
                null,
                null
        );
    }
}