package com.group.xlibris.bookWaitlist.controller;

import com.group.xlibris.bookWaitlist.dto.BookWaitlistRequest;
import com.group.xlibris.bookWaitlist.dto.BookWaitlistStatusUpdate;
import com.group.xlibris.bookWaitlist.entity.BookWaitlistEntity;
import com.group.xlibris.bookWaitlist.enums.BookWaitlistStatus;
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
class BookWaitlistControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookWaitlistController controller;

    private UUID bookId;
    private UUID bookId2;

    private UUID userId;
    private UUID userId2;

    private UUID waitlistId;
    private UUID waitlistId2;

    @BeforeEach
    void resetMap() {
        controller.clearMap();

        bookId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        bookId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        waitlistId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        waitlistId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
        userId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        BookWaitlistEntity entity = new BookWaitlistEntity(
                waitlistId,
                bookId,
                userId,
                1,
                Instant.now(),
                BookWaitlistStatus.WAITING,
                null,
                null
        );

        BookWaitlistEntity entity2 = new BookWaitlistEntity(
                waitlistId2,
                bookId,
                userId2,
                2,
                Instant.now(),
                BookWaitlistStatus.WAITING,
                null,
                null
        );

        controller.fillMap(entity);
        controller.fillMap(entity2);
    }

    @Test
    void shouldCreateWaitlistEntry() throws Exception {
        BookWaitlistRequest request = new BookWaitlistRequest(userId);

        mvc.perform(post("/api/books/{bookId}/waitlist", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldGetWaitlistByBookId() throws Exception {
        mvc.perform(get("/api/books/{bookId}/waitlist", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptyWaitlistByBookId() throws Exception {
        mvc.perform(get("/api/books/{bookId}/waitlist", bookId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldUpdateWaitListStatus() throws Exception {
        BookWaitlistStatusUpdate request = new BookWaitlistStatusUpdate(BookWaitlistStatus.NOTIFIED);

        mvc.perform(patch("/api/book-waitlists/{id}", waitlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(waitlistId.toString()))
                .andExpect(jsonPath("$.status").value("NOTIFIED"));

    }

    @Test
    void shouldDeleteWaitlistById() throws Exception {
        mvc.perform(delete("/api/book-waitlists/{id}", waitlistId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingByNonExistingId() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");

        BookWaitlistStatusUpdate request = new BookWaitlistStatusUpdate(BookWaitlistStatus.NOTIFIED);

        mvc.perform(patch("/api/book-waitlists/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingByNonExistingId() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");

        mvc.perform(delete("/api/book-waitlists/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnInvalidBodyProblemDetail() throws Exception {
        mvc.perform(post("/api/books/{bookId}/waitlist", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ abrcadabra }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));

    }
}