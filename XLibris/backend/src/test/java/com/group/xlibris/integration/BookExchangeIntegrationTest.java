package com.group.xlibris.integration;

import com.group.xlibris.IntegrationTest;
import com.group.xlibris.bookRequest.dto.BookRequestCreate;
import com.group.xlibris.bookRequest.dto.BookRequestUpdateStatus;
import com.group.xlibris.common.BookRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
public class BookExchangeIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcClient jdbc;

    private Map<String, Object> requestRow(UUID id) {
        return jdbc.sql("""
                    SELECT BIN_TO_UUID(book_id) AS book_id,
                           BIN_TO_UUID(requester_id) AS requester_id,
                           BIN_TO_UUID(owner_id) AS owner_id,
                           desired_duration_days,
                           status,
                           created_at,
                           responded_at
                    FROM requests
                    WHERE id = UUID_TO_BIN(:id)
                    """)
                .param("id", id.toString())
                .query()
                .singleRow();
    }

    @Test
    void shouldRegisterUsersCreateBookRequestAndApproveIt() throws Exception {
        String ownerEmail = UUID.randomUUID() + "@test.local";
        String password = "TestPassword123";

        String ownerResponse = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Book Owner",
                                "city", "Kyiv",
                                "email", ownerEmail,
                                "phone", "+380501234567",
                                "password", password
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID registeredOwnerId = UUID.fromString(objectMapper.readTree(ownerResponse).get("id").asString());
        var ownerRow = jdbc.sql("""
                    SELECT name, email, role
                    FROM users
                    WHERE id = UUID_TO_BIN(:id)
                    """)
                .param("id", registeredOwnerId.toString())
                .query()
                .singleRow();

        assertThat(ownerRow)
                .containsEntry("name", "Book Owner")
                .containsEntry("email", ownerEmail)
                .containsEntry("role", "USER");

        String ownerLoginResponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", ownerEmail,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ownerToken = objectMapper.readTree(ownerLoginResponse)
                .get("accessToken")
                .asString();

        String genreResponse = mvc.perform(post("/api/v1/genres")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Fantasy"
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdGenreId = UUID.fromString(objectMapper.readTree(genreResponse).get("id").asString());

        String bookResponse = mvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Scenario Book",
                                "description", "Book for integration scenario",
                                "ownerId", registeredOwnerId,
                                "genreId", createdGenreId
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdBookId = UUID.fromString(objectMapper.readTree(bookResponse).get("id").asString());

        var bookRow = jdbc.sql("""
                    SELECT title,
                           BIN_TO_UUID(owner_id) AS owner_id,
                           status
                    FROM books
                    WHERE id = UUID_TO_BIN(:id)
                    """)
                .param("id", createdBookId.toString())
                .query()
                .singleRow();

        assertThat(bookRow)
                .containsEntry("title", "Scenario Book")
                .containsEntry("owner_id", registeredOwnerId.toString())
                .containsEntry("status", "AVAILABLE");

        String requesterEmail = UUID.randomUUID() + "@test.local";
        String requesterResponse = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Book Requester",
                                "city", "Lviv",
                                "email", requesterEmail,
                                "phone", "+380671234567",
                                "password", password
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID registeredRequesterId = UUID.fromString(objectMapper.readTree(requesterResponse).get("id").asString());
        var requesterRow = jdbc.sql("""
                    SELECT email, role
                    FROM users
                    WHERE id = UUID_TO_BIN(:id)
                    """)
                .param("id", registeredRequesterId.toString())
                .query()
                .singleRow();

        assertThat(requesterRow)
                .containsEntry("email", requesterEmail)
                .containsEntry("role", "USER");

        String requesterLoginResponse = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", requesterEmail,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String requesterToken = objectMapper.readTree(requesterLoginResponse)
                .get("accessToken")
                .asString();

        BookRequestCreate createRequest = new BookRequestCreate(registeredRequesterId, 14);
        String requestResponse = mvc.perform(
                        post("/api/book-requests/{bookId}", createdBookId)
                                .header(
                                        "Authorization",
                                        "Bearer " + requesterToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        createRequest
                                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdRequestId = UUID.fromString(objectMapper.readTree(requestResponse).get("id").asString());
        var pendingRow = requestRow(createdRequestId);
        assertThat(pendingRow)
                .containsEntry("book_id", createdBookId.toString())
                .containsEntry("owner_id", registeredOwnerId.toString())
                .containsEntry("requester_id", registeredRequesterId.toString())
                .containsEntry("desired_duration_days", 14)
                .containsEntry("status", "PENDING");
        assertThat(pendingRow.get("responded_at")).isNull();

        BookRequestUpdateStatus forbiddenUpdate = new BookRequestUpdateStatus(registeredRequesterId, BookRequestStatus.APPROVED);
        mvc.perform(patch("/api/book-requests/{id}", createdRequestId)
                        .header("Authorization", "Bearer " + requesterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                forbiddenUpdate
                        )))
                .andExpect(status().isForbidden());
        assertThat(requestRow(createdRequestId)).containsEntry("status", "PENDING");

        BookRequestUpdateStatus approveRequest = new BookRequestUpdateStatus(registeredOwnerId, BookRequestStatus.APPROVED);
        mvc.perform(patch("/api/book-requests/{id}", createdRequestId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                approveRequest
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.respondedAt").exists());

        var approvedRow = requestRow(createdRequestId);
        assertThat(approvedRow)
                .containsEntry("status", "APPROVED")
                .containsEntry("book_id", createdBookId.toString())
                .containsEntry("owner_id", registeredOwnerId.toString())
                .containsEntry("requester_id", registeredRequesterId.toString());
        assertThat(approvedRow.get("responded_at")).isNotNull();

        mvc.perform(get("/api/book-requests/{id}", createdRequestId)
                        .header("Authorization", "Bearer " + requesterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdRequestId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
