package com.group.xlibris.loan.controller;

import com.group.xlibris.loan.dto.LoanRequest;
import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.loan.enums.LoanStatus;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class LoanControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanController loanController;

    @BeforeEach
    void resetMap() {
        loanController.clearMap();

        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655444000");
        UUID id2 = UUID.fromString("550e8400-e29b-41d4-a716-446655444001");

        UUID bookId = UUID.fromString("550e8400-e29b-41d4-a716-446655445000");
        UUID bookId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655445001");

        UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655446000");
        UUID ownerId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655446001");

        UUID renterId = UUID.fromString("550e8400-e29b-41d4-a716-446655447000");
        UUID renterId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655447001");


        Loan loan = new Loan(id, bookId, ownerId, renterId, Instant.now(), Instant.now().plusSeconds(86400 * 14), null, LoanStatus.ACTIVE);
        Loan loan2 = new Loan(id2, bookId2, ownerId2, renterId2, Instant.now().minusSeconds(86400 * 14), Instant.now(), null, LoanStatus.OVERDUE);

        loanController.fillMap(loan);
        loanController.fillMap(loan2);
    }

    @Test
    void shouldCreateLoan() throws Exception {
        LoanRequest request = new LoanRequest(
                null,
                UUID.fromString("550e8400-e29b-41d4-a716-446655445002"),
                UUID.fromString("550e8400-e29b-41d4-a716-446655446002"),
                UUID.fromString("550e8400-e29b-41d4-a716-446655447002"),
                Instant.now().plusSeconds(86400 * 14),
                null
        );

        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void shouldGetLoanById() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655444000");
        mvc.perform(get("/api/loans/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value("550e8400-e29b-41d4-a716-446655446000"))
                .andExpect(jsonPath("$.renterId").value("550e8400-e29b-41d4-a716-446655447000"));
    }

    @Test
    void shouldGetAllLoans() throws Exception {
        mvc.perform(get("/api/loans"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

    }

    @Test
    void shouldGetAllOverdueLoans() throws Exception {
        mvc.perform(get("/api/loans/overdue"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFilterLoansByStatus() throws Exception {
        mvc.perform(get("/api/loans")
                        .param("loanStatus", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldFilterLoansByOwnerId() throws Exception {
        mvc.perform(get("/api/loans")
                        .param("ownerId", "550e8400-e29b-41d4-a716-446655446000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerId").value("550e8400-e29b-41d4-a716-446655446000"));
    }

    @Test
    void shouldFilterLoansByRenterId() throws Exception {
        mvc.perform(get("/api/loans")
                        .param("renterId", "550e8400-e29b-41d4-a716-446655447000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].renterId").value("550e8400-e29b-41d4-a716-446655447000"));
    }

    @Test
    void shouldAssignToReturned() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655444000");

        mvc.perform(patch("/api/loans/{id}/return", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.actualReturnDate").exists());
    }

    @Test
    void shouldDeleteLoan() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655444001");

        mvc.perform(delete("/api/loans/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ Invalid body }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }

    @Test
    void shouldReturnValidationProblemDetail() throws Exception {
        LoanRequest invalidRequest = new LoanRequest(
                null,
                null,
                UUID.fromString("550e8400-e29b-41d4-a716-446655446002"),
                UUID.fromString("550e8400-e29b-41d4-a716-446655447002"),
                Instant.now().minusSeconds(86400),
                null
        );

        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.bookId").exists())
                .andExpect(jsonPath("$.errors.expectedReturnDate").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentLoan() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655444003");
        mvc.perform(get("/api/loans/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentLoan() throws Exception {
        UUID nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655444003");

        mvc.perform(delete("/api/loans/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnConflictWhenAssigningNonActiveLoanToReturned() throws Exception {
        UUID overdueLoanId = UUID.fromString("550e8400-e29b-41d4-a716-446655444001");

        mvc.perform(patch("/api/loans/{id}/return", overdueLoanId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Business rule violation"))
                .andExpect(jsonPath("$.detail").value("Only loans in ACTIVE status can be returned"));
    }

    @Test
    void shouldRejectUnknownPropertiesInJson() throws Exception {
        String jsonWithUnknownField = """
                {
                    "bookId": "550e8400-e29b-41d4-a716-446655445002",
                    "ownerId": "550e8400-e29b-41d4-a716-446655446002",
                    "renterId": "550e8400-e29b-41d4-a716-446655447002",
                    "expectedReturnDate": "2030-01-01T00:00:00Z",
                    "unexpectedField": "suspiciousValue"
                }
                """;

        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithUnknownField))
                .andExpect(status().isBadRequest());
    }
}
