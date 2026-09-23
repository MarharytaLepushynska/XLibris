package com.group.xlibris.bookRequest.controller;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import com.group.xlibris.book.repository.BookRepository;
import com.group.xlibris.bookRequest.dto.BookRequestCreate;
import com.group.xlibris.bookRequest.dto.BookRequestUpdateStatus;
import com.group.xlibris.bookRequest.internal.BookRequestEntity;
import com.group.xlibris.landCommon.BookRequestStatus;
import com.group.xlibris.bookRequest.internal.BookRequestRepository;
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
class BookRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRequestRepository repository;

    @Autowired
    private BookRepository bookRepository;

    private UUID requestId;
    private UUID requestId2;

    private UUID bookId;
    private UUID bookId2;

    private UUID requesterId;
    private UUID requesterId2;

    private UUID ownerId;

    @BeforeEach
    void resetMap() {
        repository.deleteAll();
        bookRepository.deleteAll();

        requestId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        requestId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        bookId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        bookId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        requesterId = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        requesterId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
        ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        BookRequestEntity entity = new BookRequestEntity(
                requestId,
                bookId,
                requesterId,
                ownerId,
                14,
                BookRequestStatus.PENDING,
                Instant.now(),
                null
        );

        BookRequestEntity entity2 = new BookRequestEntity(
                requestId2,
                bookId2,
                requesterId2,
                ownerId,
                21,
                BookRequestStatus.APPROVED,
                Instant.now(),
                null
        );

        Book book = new Book(
                bookId,
                "Test book",
                "Test desc",
                null,
                BookStatus.AVAILABLE,
                ownerId,
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        bookRepository.save(book);

        repository.save(entity);
        repository.save(entity2);
    }

    @Test
    void shouldCreateRequest() throws Exception {
        BookRequestCreate request = new BookRequestCreate(requesterId2, 21);

        mvc.perform(post("/api/book-requests/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.requesterId").value(requesterId2.toString()))
                .andExpect(jsonPath("$.desiredDurationDays").value(21))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        mvc.perform(get("/api/book-requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.requesterId").value(requesterId.toString()))
                .andExpect(jsonPath("$.desiredDurationDays").value(14))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldGetAllRequest() throws Exception {
        mvc.perform(get("/api/book-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFilterRequestsByBookId() throws Exception {
        mvc.perform(get("/api/book-requests")
                .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(bookId.toString()));
    }

    @Test
    void shouldFilterRequestsByRequesterId() throws Exception {
        mvc.perform(get("/api/book-requests")
                        .param("requesterId", requesterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].requesterId").value(requesterId.toString()));
    }

    @Test
    void shouldFilterRequestsByStatus() throws Exception {
        mvc.perform(get("/api/book-requests")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440009");
        mvc.perform(get("/api/book-requests")
                .param("bookId", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldUpdateRequestStatus() throws Exception {
        BookRequestUpdateStatus request = new BookRequestUpdateStatus(ownerId, BookRequestStatus.APPROVED);

        mvc.perform(patch("/api/book-requests/{id}", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.respondedAt").exists());
    }

    @Test
    void shouldDeleteRequestById() throws Exception {
        mvc.perform(delete("/api/book-requests/{id}", requestId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotfoundForUnknownId() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440009");

        mvc.perform(get("/api/book-requests/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingWithUnknownId() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440009");

        BookRequestUpdateStatus request = new BookRequestUpdateStatus(ownerId, BookRequestStatus.APPROVED);

        mvc.perform(patch("/api/book-requests/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingWithUnknownId() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440009");

        mvc.perform(delete("/api/book-requests/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnInvalidRequestBodyProblemDetail() throws Exception {
        mvc.perform(post("/api/book-requests/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ blablabla }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }
}