package com.group.xlibris.integration;

import com.group.xlibris.IntegrationTest;

import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookStatus;

import com.group.xlibris.loan.internal.Loan;

import com.group.xlibris.security.jwt.JwtService;

import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.internal.UserRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@IntegrationTest
@Transactional
class FeedbackIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;


    private UUID createUser() {

        UUID id = UUID.randomUUID();

        User user = new User(
                id,
                "Feedback Test",
                "Kyiv",
                null,
                id + "@test.local",
                "000",
                Instant.now(),
                Role.USER,
                "test",
                0.0,
                0.0,
                0,
                0,
                0
        );

        userRepository.saveAndFlush(user);

        return id;
    }


    private String generateToken(UUID userId) {

        String email = userId + "@test.local";

        var userDetails =
                userDetailsService.loadUserByUsername(email);

        return jwtService.generateToken(userDetails);
    }


    private UUID createReturnedLoan(UUID ownerId, UUID renterId) {

        User owner = userRepository.findById(ownerId).orElseThrow();
        User renter = userRepository.findById(renterId).orElseThrow();

        Book book = new Book(
                UUID.randomUUID(),
                "Test Book",
                "Test Description",
                null,
                BookStatus.AVAILABLE,
                owner,
                null,
                null
        );

        entityManager.persist(book);

        Instant now = Instant.now();

        Loan loan = new Loan(
                UUID.randomUUID(),
                book,
                owner,
                renter,
                now.minusSeconds(86400),
                now.plusSeconds(86400),
                now
        );

        entityManager.persist(loan);

        entityManager.flush();

        return loan.getId();
    }


    private String feedbackJson(
            UUID loanId,
            UUID reviewerId,
            UUID reviewedUserId,
            int rating
    ) {

        return """
                {
                    "loanId": "%s",
                    "reviewerId": "%s",
                    "reviewedUserId": "%s",
                    "rating": %d,
                    "comment": "Helpful reader"
                }
                """.formatted(
                loanId,
                reviewerId,
                reviewedUserId,
                rating
        );
    }


    @Test
    void createReadFilterFeedback_verifyDatabase() throws Exception {

        UUID ownerId = createUser();
        UUID renterId = createUser();

        UUID loanId = createReturnedLoan(ownerId, renterId);

        String token = generateToken(ownerId);

        String json = feedbackJson(
                loanId,
                ownerId,
                renterId,
                5
        );

        String response = mvc.perform(
                        post("/api/feedbacks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID feedbackId = UUID.fromString(
                mapper.readTree(response).get("id").asText()
        );

        entityManager.flush();

        Integer savedRating = jdbc.sql("""
                SELECT rating
                FROM feedbacks
                WHERE id = UUID_TO_BIN(:id)
                """)
                .param("id", feedbackId.toString())
                .query(Integer.class)
                .single();

        assertThat(savedRating).isEqualTo(5);

        String savedComment = jdbc.sql("""
                SELECT comment
                FROM feedbacks
                WHERE id = UUID_TO_BIN(:id)
                """)
                .param("id", feedbackId.toString())
                .query(String.class)
                .single();

        assertThat(savedComment).isEqualTo("Helpful reader");

        String savedLoanId = jdbc.sql("""
                SELECT BIN_TO_UUID(loan_id)
                FROM feedbacks
                WHERE id = UUID_TO_BIN(:id)
                """)
                .param("id", feedbackId.toString())
                .query(String.class)
                .single();

        assertThat(savedLoanId)
                .isEqualToIgnoringCase(loanId.toString());

        mvc.perform(
                        get("/api/feedbacks/{id}", feedbackId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment")
                        .value("Helpful reader"))
                .andExpect(jsonPath("$.rating").value(5));

        mvc.perform(
                        get("/api/feedbacks")
                                .header("Authorization", "Bearer " + token)
                                .param("loanId", loanId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(feedbackId.toString()));
    }


    @Test
    void duplicateFeedback_returns409() throws Exception {

        UUID ownerId = createUser();
        UUID renterId = createUser();

        UUID loanId = createReturnedLoan(ownerId, renterId);

        String token = generateToken(ownerId);

        String json = feedbackJson(
                loanId,
                ownerId,
                renterId,
                5
        );

        mvc.perform(
                        post("/api/feedbacks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());

        entityManager.flush();

        mvc.perform(
                        post("/api/feedbacks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isConflict());

        Long count = jdbc.sql("""
                SELECT COUNT(*)
                FROM feedbacks
                WHERE loan_id = UUID_TO_BIN(:loanId)
                """)
                .param("loanId", loanId.toString())
                .query(Long.class)
                .single();

        assertThat(count).isEqualTo(1);
    }


    @Test
    void selfFeedback_returns400() throws Exception {

        UUID ownerId = createUser();
        UUID renterId = createUser();

        UUID loanId = createReturnedLoan(ownerId, renterId);

        String token = generateToken(ownerId);

        String json = feedbackJson(
                loanId,
                ownerId,
                ownerId,
                5
        );

        mvc.perform(
                        post("/api/feedbacks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        Long count = jdbc.sql("""
                SELECT COUNT(*)
                FROM feedbacks
                WHERE loan_id = UUID_TO_BIN(:loanId)
                """)
                .param("loanId", loanId.toString())
                .query(Long.class)
                .single();

        assertThat(count).isZero();
    }


    @Test
    void invalidRating_returns400() throws Exception {

        UUID ownerId = createUser();
        UUID renterId = createUser();

        UUID loanId = createReturnedLoan(ownerId, renterId);

        String token = generateToken(ownerId);

        String json = feedbackJson(
                loanId,
                ownerId,
                renterId,
                6
        );

        mvc.perform(
                        post("/api/feedbacks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        Long count = jdbc.sql("""
                SELECT COUNT(*)
                FROM feedbacks
                WHERE loan_id = UUID_TO_BIN(:loanId)
                """)
                .param("loanId", loanId.toString())
                .query(Long.class)
                .single();

        assertThat(count).isZero();
    }
}