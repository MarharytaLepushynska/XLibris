package com.group.xlibris.integration;

import com.group.xlibris.IntegrationTest;

import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.report.ReportType;
import com.group.xlibris.report.internal.Report;
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

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@IntegrationTest
@Transactional
class ReportIntegrationTest {

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

    private UUID createUser(Role role) {
        UUID id = UUID.randomUUID();
        User user = new User(
                id, "Report Test", "Kyiv",
                null, id + "@test.local", "000",
                Instant.now(), role, "test",
                0.0, 0.0,
                0, 0, 0);

        userRepository.saveAndFlush(user);
        return id;
    }

    private UUID createUser() {
        return createUser(Role.USER);
    }

    private String generateToken(UUID userId) {

        String email = userId + "@test.local";

        var userDetails =
                userDetailsService.loadUserByUsername(email);

        return jwtService.generateToken(userDetails);
    }

    private UUID createLoan(UUID ownerId, UUID renterId) {
        User owner = userRepository.findById(ownerId).orElseThrow();
        User renter = userRepository.findById(renterId).orElseThrow();

        Book book = new Book(UUID.randomUUID(), "Test Book",
                "Test Description", null, BookStatus.BORROWED,
                owner, null, null);

        entityManager.persist(book);
        Instant now = Instant.now();
        Loan loan = new Loan(UUID.randomUUID(),
                book, owner, renter,
                now.minusSeconds(86400), now.plusSeconds(86400), null);

        entityManager.persist(loan);
        entityManager.flush();
        return loan.getId();
    }

    private UUID createPendingReport(UUID reporterId, UUID targetId) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();

        Report report = Report.standalone(
                "Seeded report",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Seeded description",
                URI.create("https://example.com/seed.png"),
                reporter,
                target
        );

        entityManager.persist(report);
        entityManager.flush();
        return report.getId();
    }

    private String reportJson(UUID reporterId, UUID targetUserId, String title) {
        return """
                {
                    "title": "%s",
                    "type": "DAMAGED_BOOK",
                    "description": "Cover is torn",
                    "evidenceUrl": "https://example.com/evidence.png",
                    "reporterId": "%s",
                    "targetUserId": "%s"
                }
                """.formatted(title, reporterId, targetUserId);
    }

    private Long countReportsByReporter(UUID reporterId) {
        return jdbc.sql("""
                        SELECT COUNT(*)
                        FROM reports
                        WHERE reporter_id = UUID_TO_BIN(:id)
                        """)
                .param("id", reporterId.toString())
                .query(Long.class)
                .single();
    }

    private String reportStatus(UUID reportId) {
        return jdbc.sql("""
                        SELECT status
                        FROM reports
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", reportId.toString())
                .query(String.class)
                .single();
    }

    @Test
    void createStandaloneReport_verifyDatabase() throws Exception {
        UUID reporterId = createUser();
        UUID targetId = createUser();

        String token = generateToken(reporterId);
        String response = mvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJson(reporterId, targetId, "Torn cover")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.title").value("Torn cover"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID reportId = UUID.fromString(
                mapper.readTree(response).get("id").asString()
        );

        entityManager.flush();

        Map<String, Object> row = jdbc.sql("""
                        SELECT title, type, status,
                               BIN_TO_UUID(reporter_id) AS reporter_id,
                               BIN_TO_UUID(target_user_id) AS target_user_id,
                               loan_id
                        FROM reports
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", reportId.toString())
                .query()
                .singleRow();

        assertThat(row.get("title")).isEqualTo("Torn cover");
        assertThat(row.get("type")).isEqualTo("DAMAGED_BOOK");
        assertThat(row.get("status")).isEqualTo("PENDING");
        assertThat(row.get("reporter_id").toString())
                .isEqualToIgnoringCase(reporterId.toString());
        assertThat(row.get("target_user_id").toString())
                .isEqualToIgnoringCase(targetId.toString());
        assertThat(row.get("loan_id")).isNull();
    }

    @Test
    void createLoanReport_targetIsOtherParticipant_verifyDatabase() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID loanId = createLoan(ownerId, renterId);

        String token = generateToken(ownerId);
        String json = """
                {
                    "title": "Book not returned",
                    "type": "NO_RETURN",
                    "description": "Renter does not answer",
                    "evidenceUrl": "https://example.com/chat.png",
                    "reporterId": "%s"
                }
                """.formatted(ownerId);

        String response = mvc.perform(post("/api/loans/{loanId}/reports", loanId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").value(loanId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID reportId = UUID.fromString(
                mapper.readTree(response).get("id").asString()
        );
        entityManager.flush();

        Map<String, Object> row = jdbc.sql("""
                        SELECT BIN_TO_UUID(loan_id) AS loan_id,
                               BIN_TO_UUID(target_user_id) AS target_user_id
                        FROM reports
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", reportId.toString())
                .query()
                .singleRow();

        assertThat(row.get("loan_id").toString())
                .isEqualToIgnoringCase(loanId.toString());
        assertThat(row.get("target_user_id").toString())
                .isEqualToIgnoringCase(renterId.toString());
    }

    @Test
    void createLoanReport_byNonParticipant_returns400() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID strangerId = createUser();
        UUID loanId = createLoan(ownerId, renterId);

        String token = generateToken(strangerId);
        String json = """
                {
                    "title": "Not my loan",
                    "type": "NO_RETURN",
                    "description": "Should be rejected",
                    "evidenceUrl": "https://example.com/x.png",
                    "reporterId": "%s"
                }
                """.formatted(strangerId);

        mvc.perform(post("/api/loans/{loanId}/reports", loanId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
        assertThat(countReportsByReporter(strangerId)).isZero();
    }

    @Test
    void moderatorReviewsAndResolvesReport_verifyDatabase() throws Exception {
        UUID reporterId = createUser();
        UUID targetId = createUser();
        UUID moderatorId = createUser(Role.MODERATOR);
        UUID reportId = createPendingReport(reporterId, targetId);

        String token = generateToken(moderatorId);

        mvc.perform(get("/api/reports/{id}", reportId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mvc.perform(patch("/api/reports/{id}/review", reportId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));

        entityManager.flush();
        assertThat(reportStatus(reportId)).isEqualTo("IN_REVIEW");

        String resolution = """
                {
                    "resolution": "RESOLVED",
                    "moderatorComment": "Warning issued to the user",
                    "moderatorVerdict": "WARNING"
                }
                """;

        mvc.perform(patch("/api/reports/{id}/resolution", reportId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolution))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.moderatorVerdict").value("WARNING"));
        entityManager.flush();

        Map<String, Object> row = jdbc.sql("""
                        SELECT status, moderator_comment, moderator_verdict
                        FROM reports
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", reportId.toString())
                .query()
                .singleRow();

        assertThat(row.get("status")).isEqualTo("RESOLVED");
        assertThat(row.get("moderator_comment"))
                .isEqualTo("Warning issued to the user");
        assertThat(row.get("moderator_verdict")).isEqualTo("WARNING");
    }

    @Test
    void resolvePendingReportWithoutReview_returns422() throws Exception {
        UUID reporterId = createUser();
        UUID targetId = createUser();
        UUID moderatorId = createUser(Role.MODERATOR);
        UUID reportId = createPendingReport(reporterId, targetId);

        String token = generateToken(moderatorId);
        String resolution = """
                {
                    "resolution": "RESOLVED",
                    "moderatorComment": "Skipping the review step",
                    "moderatorVerdict": "WARNING"
                }
                """;

        mvc.perform(patch("/api/reports/{id}/resolution", reportId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolution))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());

        entityManager.flush();
        assertThat(reportStatus(reportId)).isEqualTo("PENDING");
    }

    @Test
    void reporterUpdatesPendingReport_verifyDatabase() throws Exception {
        UUID reporterId = createUser();
        UUID targetId = createUser();

        UUID reportId = createPendingReport(reporterId, targetId);

        String token = generateToken(reporterId);

        String json = """
                {
                    "title": "Updated title",
                    "type": "OVERDUE_RETURN",
                    "description": "Updated description",
                    "evidenceUrl": "https://example.com/new.png"
                }
                """;

        mvc.perform(patch("/api/reports/{id}", reportId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));
        entityManager.flush();

        Map<String, Object> row = jdbc.sql("""
                        SELECT title, type, description
                        FROM reports
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", reportId.toString())
                .query()
                .singleRow();

        assertThat(row.get("title")).isEqualTo("Updated title");
        assertThat(row.get("type")).isEqualTo("OVERDUE_RETURN");
        assertThat(row.get("description")).isEqualTo("Updated description");
    }

    @Test
    void moderatorDeletesReport_removesRowFromDatabase() throws Exception {
        UUID reporterId = createUser();
        UUID targetId = createUser();
        UUID moderatorId = createUser(Role.MODERATOR);
        UUID reportId = createPendingReport(reporterId, targetId);

        String token = generateToken(moderatorId);
        assertThat(countReportsByReporter(reporterId)).isEqualTo(1);

        mvc.perform(delete("/api/reports/{id}", reportId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        entityManager.flush();
        assertThat(countReportsByReporter(reporterId)).isZero();
    }

    @Test
    void selfReport_returns400() throws Exception {
        UUID userId = createUser();
        String token = generateToken(userId);

        mvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJson(userId, userId, "Self report")))
                .andDo(print())
                .andExpect(status().isBadRequest());
        assertThat(countReportsByReporter(userId)).isZero();
    }

    @Test
    void blankTitle_returns400() throws Exception {
        UUID reporterId = createUser();
        UUID targetId = createUser();
        String token = generateToken(reporterId);

        mvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJson(reporterId, targetId, "")))
                .andDo(print())
                .andExpect(status().isBadRequest());
        assertThat(countReportsByReporter(reporterId)).isZero();
    }

    @Test
    void regularUserCannotReadReports_returns403() throws Exception {
        UUID reporterId = createUser();
        UUID targetId = createUser();
        UUID reportId = createPendingReport(reporterId, targetId);

        String token = generateToken(reporterId);
        mvc.perform(get("/api/reports/{id}", reportId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void readReports_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/reports"))
                .andExpect(status().isUnauthorized());
    }
}