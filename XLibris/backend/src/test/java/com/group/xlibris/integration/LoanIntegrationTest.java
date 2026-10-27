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

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@IntegrationTest
@Transactional
class LoanIntegrationTest {

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
        User user = new User(id, "Loan Test", "Kyiv",
                null, id + "@test.local", "000", Instant.now(),
                Role.USER, "test", 0.0, 0.0,
                0, 0, 0);
        userRepository.saveAndFlush(user);
        return id;
    }


    private String generateToken(UUID userId) {
        String email = userId + "@test.local";
        var userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateToken(userDetails);
    }


    private UUID createBook(UUID ownerId, BookStatus status) {
        User owner = userRepository.findById(ownerId).orElseThrow();
        Book book = new Book(UUID.randomUUID(), "Test Book",
                "Test Description", null,
                status, owner, null, null);

        entityManager.persist(book);
        entityManager.flush();
        return book.getId();
    }


    private UUID createLoan(UUID bookId, UUID ownerId, UUID renterId, Instant expectedReturn, Instant actualReturn) {
        Book book = entityManager.find(Book.class, bookId);
        User owner = userRepository.findById(ownerId).orElseThrow();
        User renter = userRepository.findById(renterId).orElseThrow();

        Loan loan = new Loan(UUID.randomUUID(), book,
                owner, renter,
                Instant.now().minusSeconds(86400),
                expectedReturn, actualReturn
        );

        entityManager.persist(loan);
        entityManager.flush();
        return loan.getId();
    }


    private String loanJson(UUID bookId, UUID ownerId, UUID renterId, Instant expectedReturn) {
        return """
                {
                    "bookId": "%s",
                    "ownerId": "%s",
                    "renterId": "%s",
                    "expectedReturnDate": "%s"
                }
                """.formatted(bookId, ownerId, renterId, expectedReturn);
    }


    private Long countLoansByOwner(UUID ownerId) {
        return jdbc.sql("""
                        SELECT COUNT(*)
                        FROM loans
                        WHERE owner_id = UUID_TO_BIN(:ownerId)
                        """)
                .param("ownerId", ownerId.toString())
                .query(Long.class)
                .single();
    }


    @Test
    void createReadFilterLoan_verifyDatabase() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID bookId = createBook(ownerId, BookStatus.AVAILABLE);

        String token = generateToken(renterId);

        String json = loanJson(bookId, ownerId, renterId,
                Instant.now().plus(Duration.ofDays(7)));

        String response = mvc.perform(post("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID loanId = UUID.fromString(mapper.readTree(response).get("id").asString());
        entityManager.flush();

        Map<String, Object> row = jdbc.sql("""
                        SELECT BIN_TO_UUID(book_id) AS book_id,
                               BIN_TO_UUID(owner_id) AS owner_id,
                               BIN_TO_UUID(renter_id) AS renter_id,
                               actual_return_date
                        FROM loans
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", loanId.toString())
                .query()
                .singleRow();

        assertThat(row.get("book_id").toString())
                .isEqualToIgnoringCase(bookId.toString());
        assertThat(row.get("owner_id").toString())
                .isEqualToIgnoringCase(ownerId.toString());
        assertThat(row.get("renter_id").toString())
                .isEqualToIgnoringCase(renterId.toString());
        assertThat(row.get("actual_return_date")).isNull();

        mvc.perform(get("/api/loans/{id}", loanId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(get("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .param("renterId", renterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(loanId.toString()));
    }


    @Test
    void filterLoansByStatus_returnsOnlyOverdue() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID activeBookId = createBook(ownerId, BookStatus.BORROWED);
        UUID overdueBookId = createBook(ownerId, BookStatus.BORROWED);

        createLoan(activeBookId, ownerId, renterId, Instant.now().plus(Duration.ofDays(5)), null);
        UUID overdueLoanId = createLoan(overdueBookId, ownerId, renterId, Instant.now().minus(Duration.ofHours(2)), null);

        String token = generateToken(renterId);
        mvc.perform(get("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .param("ownerId", ownerId.toString())
                        .param("loanStatus", "OVERDUE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(overdueLoanId.toString()))
                .andExpect(jsonPath("$[0].status").value("OVERDUE"));

        assertThat(countLoansByOwner(ownerId)).isEqualTo(2);
    }


    @Test
    void returnLoan_setsReturnDateAndMakesBookAvailable() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID bookId = createBook(ownerId, BookStatus.BORROWED);
        UUID loanId = createLoan(bookId, ownerId, renterId, Instant.now().plus(Duration.ofDays(5)), null);

        String token = generateToken(renterId);
        mvc.perform(patch("/api/loans/{id}/return", loanId)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        entityManager.flush();

        Long returned = jdbc.sql("""
                        SELECT COUNT(*)
                        FROM loans
                        WHERE id = UUID_TO_BIN(:id)
                          AND actual_return_date IS NOT NULL
                        """)
                .param("id", loanId.toString())
                .query(Long.class)
                .single();

        assertThat(returned).isEqualTo(1);

        String bookStatus = jdbc.sql("""
                        SELECT status
                        FROM books
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", bookId.toString())
                .query(String.class)
                .single();

        assertThat(bookStatus).isEqualTo("AVAILABLE");
    }


    @Test
    void returnLoan_whenAlreadyReturned_returns422() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID bookId = createBook(ownerId, BookStatus.AVAILABLE);
        UUID loanId = createLoan(bookId, ownerId, renterId, Instant.now().plus(Duration.ofDays(5)), Instant.now());

        String token = generateToken(renterId);
        mvc.perform(patch("/api/loans/{id}/return", loanId)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }


    @Test
    void deleteLoan_removesRowFromDatabase() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID bookId = createBook(ownerId, BookStatus.BORROWED);
        UUID loanId = createLoan(bookId, ownerId, renterId, Instant.now().plus(Duration.ofDays(5)), null);

        String token = generateToken(ownerId);
        assertThat(countLoansByOwner(ownerId)).isEqualTo(1);

        mvc.perform(delete("/api/loans/{id}", loanId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        entityManager.flush();
        assertThat(countLoansByOwner(ownerId)).isZero();
    }


    @Test
    void createLoan_withPastReturnDate_returns400() throws Exception {
        UUID ownerId = createUser();
        UUID renterId = createUser();
        UUID bookId = createBook(ownerId, BookStatus.AVAILABLE);

        String token = generateToken(renterId);

        String json = loanJson(bookId, ownerId, renterId, Instant.now().minus(Duration.ofDays(1)));

        mvc.perform(post("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
        assertThat(countLoansByOwner(ownerId)).isZero();
    }


    @Test
    void createLoan_whenOwnerEqualsRenter_returns400() throws Exception {
        UUID ownerId = createUser();
        UUID bookId = createBook(ownerId, BookStatus.AVAILABLE);

        String token = generateToken(ownerId);
        String json = loanJson(bookId, ownerId, ownerId, Instant.now().plus(Duration.ofDays(7)));

        mvc.perform(post("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
        assertThat(countLoansByOwner(ownerId)).isZero();
    }


    @Test
    void getUnknownLoan_returns404() throws Exception {
        UUID userId = createUser();
        String token = generateToken(userId);
        mvc.perform(get("/api/loans/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }


    @Test
    void getLoans_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/loans"))
                .andExpect(status().isUnauthorized());
    }
}