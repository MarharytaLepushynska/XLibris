package com.group.xlibris.bookRequest.controller;

import com.group.xlibris.IntegrationTest;
import com.group.xlibris.author.Author;
import com.group.xlibris.author.AuthorRepository;
import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.bookRequest.dto.BookRequestCreate;
import com.group.xlibris.bookRequest.dto.BookRequestUpdateStatus;
import com.group.xlibris.bookRequest.internal.BookRequestEntity;
import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.bookRequest.internal.BookRequestRepository;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.internal.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@WithMockUser(username="test@test.local", roles="USER")
@AutoConfigureMockMvc(addFilters = false)
class BookRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRequestRepository repository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private JdbcClient jdbc;

    private UUID requestId;
    private UUID bookId;
    private UUID requesterId;
    private UUID requesterId2;
    private UUID ownerId;

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

    private long countRequests() {
        return jdbc.sql("SELECT COUNT(*) FROM requests")
                .query(Long.class)
                .single();
    }

    private long countRequest(UUID id) {
        return jdbc.sql("""
                    SELECT COUNT(*)
                    FROM requests
                    WHERE id = UUID_TO_BIN(:id)
                    """)
                .param("id", id.toString())
                .query(Long.class)
                .single();
    }

    @BeforeEach
    void resetMap() {
        repository.deleteAll();
        bookRepository.deleteAll();

        requestId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID requestId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        bookId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UUID bookId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        requesterId = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        requesterId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
        ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");
        UUID authorId = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");
        UUID genreId = UUID.fromString("550e8400-e29b-41d4-a716-446655440008");
        UUID borrowerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440009");

        User user = new User(ownerId, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER, "TestPassword",
                2.0, 1.9, 4, 5, 1);

        User user2 = new User(borrowerId, "Max", "Lviv", null, "max@gmail.com",
                "+380998876447", Instant.now(), Role.USER, "TestPassword",
                4.0, 2.9, 5, 6, 0);
        User requester = new User(requesterId, "Anna", "Lviv", null, "anna@gmail.com",
                "+380998876447", Instant.now(), Role.USER, "TestPassword",
                4.0, 2.9, 5, 6, 0);
        User requester2 = new User(requesterId2, "Ivan", "Lviv", null, "ivan@gmail.com",
                "+380998876447", Instant.now(), Role.USER, "TestPassword",
                4.0, 2.9, 5, 6, 0);
        userRepository.save(user);
        userRepository.save(user2);
        userRepository.save(requester);
        userRepository.save(requester2);

        Author author = new Author(authorId, "JK Rowling");
        Genre genre = new Genre(genreId, "Horror");
        authorRepository.save(author);
        genreRepository.save(genre);

        Book book = new Book(
                bookId,
                "Test book",
                "Test desc",
                null,
                BookStatus.AVAILABLE,
                user,
                author,
                genre
        );

        Book book2 = new Book(
                bookId2,
                "Test book2",
                "Test desc2",
                null,
                BookStatus.AVAILABLE,
                user2,
                author,
                genre
        );
        bookRepository.save(book);
        bookRepository.save(book2);

        Instant createdAt = Instant.now();

        repository.save(new BookRequestEntity(
                requestId,
                book,
                requester,
                user,
                14,
                BookRequestStatus.PENDING,
                createdAt,
                null
        ));

        repository.save(new BookRequestEntity(
                requestId2,
                book2,
                requester2,
                user2,
                21,
                BookRequestStatus.APPROVED,
                createdAt,
                createdAt
        ));
    }

    @Test
    void shouldCreateRequest() throws Exception {
        BookRequestCreate request = new BookRequestCreate(requesterId2, 21);
        long before = countRequests();

        String response = mvc.perform(
                        post("/api/book-requests/{bookId}", bookId)
                                .with(user("ivan@gmail.com").roles("USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(
                        jsonPath("$.requesterId")
                                .value(requesterId2.toString())
                )
                .andExpect(jsonPath("$.desiredDurationDays").value(21))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdId = UUID.fromString(
                objectMapper.readTree(response).get("id").asString()
        );

        Map<String, Object> row = requestRow(createdId);
        assertThat(countRequests()).isEqualTo(before + 1);
        assertThat(row)
                .containsEntry("book_id", bookId.toString())
                .containsEntry("requester_id", requesterId2.toString())
                .containsEntry("owner_id", ownerId.toString())
                .containsEntry("desired_duration_days", 21)
                .containsEntry("status", "PENDING");

        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("responded_at")).isNull();
    }

    @Test
    void shouldGetRequestById() throws Exception {
        Map<String, Object> row = requestRow(requestId);

        mvc.perform(get("/api/book-requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(
                        jsonPath("$.requesterId")
                                .value(row.get("requester_id"))
                )
                .andExpect(
                        jsonPath("$.desiredDurationDays")
                                .value(row.get("desired_duration_days"))
                )
                .andExpect(jsonPath("$.status").value(row.get("status")));

        assertThat(countRequest(requestId)).isEqualTo(1);
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

        Map<String, Object> row = requestRow(requestId);

        assertThat(row)
                .containsEntry("status", "APPROVED")
                .containsEntry("book_id", bookId.toString())
                .containsEntry("requester_id", requesterId.toString())
                .containsEntry("owner_id", ownerId.toString());

        assertThat(row.get("responded_at")).isNotNull();
    }

    @Test
    void shouldDeleteRequestById() throws Exception {
        long before = countRequests();
        assertThat(countRequest(requestId)).isEqualTo(1);

        mvc.perform(delete("/api/book-requests/{id}", requestId))
                .andExpect(status().isNoContent());

        assertThat(countRequest(requestId)).isZero();
        assertThat(countRequests()).isEqualTo(before - 1);
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

    @Test
    void shouldRejectDuplicateRequest() throws Exception {
        BookRequestCreate request = new BookRequestCreate(requesterId, 14);
        long before = countRequests();

        mvc.perform(post("/api/book-requests/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        assertThat(countRequests()).isEqualTo(before);
        assertThat(requestRow(requestId)).containsEntry("status", "PENDING");
    }

    @Test
    void shouldRejectInvalidDuration() throws Exception {
        BookRequestCreate request = new BookRequestCreate(requesterId2, 0);
        long before = countRequests();

        mvc.perform(post("/api/book-requests/{bookId}", bookId)
                        .with(user("ivan@gmail.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(countRequests()).isEqualTo(before);
    }
}