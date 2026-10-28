
package com.group.xlibris.book;

import com.group.xlibris.IntegrationTest;
import com.group.xlibris.author.Author;
import com.group.xlibris.author.AuthorRepository;
import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.security.dto.LoginRequest;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.internal.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class BookControllerTest {

    private static final String TEST_PASSWORD = "TestPass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID bookId;
    private UUID ownerId;
    private UUID authorId;
    private UUID genreId;

    private User user;
    private Author author;
    private Genre genre;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        bookId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        genreId = UUID.randomUUID();

        String email = "book-test-" + ownerId + "@example.com";

        user = new User(
                ownerId,
                "Marta",
                "Kyiv",
                null,
                email,
                "+380998876446",
                Instant.now(),
                Role.USER,
                passwordEncoder.encode(TEST_PASSWORD),
                2.0,
                1.9,
                4,
                5,
                1
        );

        author = new Author(authorId, "JK Rowling");
        genre = new Genre(genreId, "Horror");

        user = userRepository.save(user);
        author = authorRepository.save(author);
        genre = genreRepository.save(genre);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(email, TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = objectMapper.readTree(loginResponse)
                .get("accessToken")
                .asText();
    }

    private byte[] uuidToBytes(UUID id) {
        return ByteBuffer.allocate(16)
                .putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits())
                .array();
    }

    @Test
    void createBook() throws Exception {
        BookRequest request = new BookRequest(
                null,
                "Test Book",
                "Test description",
                "photo.jpg",
                ownerId,
                authorId,
                genreId
        );

        String response = mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.photoURL").value("photo.jpg"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.authorId").value(authorId.toString()))
                .andExpect(jsonPath("$.genreId").value(genreId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdBookId = UUID.fromString(
                objectMapper.readTree(response).get("id").asString()
        );

        String savedTitle = jdbcClient.sql(
                        "SELECT title FROM books WHERE id = :id")
                .param("id", uuidToBytes(createdBookId))
                .query(String.class)
                .single();

        assertEquals("Test Book", savedTitle);

        String savedStatus = jdbcClient.sql(
                        "SELECT status FROM books WHERE id = :id")
                .param("id", uuidToBytes(createdBookId))
                .query(String.class)
                .single();

        assertEquals("AVAILABLE", savedStatus);

        Integer count = jdbcClient.sql(
                        "SELECT COUNT(*) FROM books WHERE id = :id")
                .param("id", uuidToBytes(createdBookId))
                .query(Integer.class)
                .single();

        assertEquals(1, count);
    }

    @Test
    void getBookById() throws Exception {
        Book book = new Book(
                bookId,
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.AVAILABLE,
                user,
                author,
                genre
        );

        bookRepository.save(book);

        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.photoURL").value("photo.jpg"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.authorId").value(authorId.toString()))
                .andExpect(jsonPath("$.genreId").value(genreId.toString()));

        String savedTitle = jdbcClient.sql(
                        "SELECT title FROM books WHERE id = :id")
                .param("id", uuidToBytes(bookId))
                .query(String.class)
                .single();

        assertEquals("Test Book", savedTitle);
    }

    @Test
    void getAllBooks() throws Exception {
        Book book1 = new Book(
                UUID.randomUUID(),
                "Book One",
                "Description one",
                "photo1.jpg",
                BookStatus.AVAILABLE,
                user,
                author,
                genre
        );

        Book book2 = new Book(
                UUID.randomUUID(),
                "Book Two",
                "Description two",
                "photo2.jpg",
                BookStatus.BORROWED,
                user,
                author,
                genre
        );

        bookRepository.save(book1);
        bookRepository.save(book2);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id")
                        .value(org.hamcrest.Matchers.hasItems(
                                book1.getId().toString(),
                                book2.getId().toString()
                        )));

        Integer count = jdbcClient.sql(
                        "SELECT COUNT(*) FROM books WHERE id IN (:id1, :id2)")
                .param("id1", uuidToBytes(book1.getId()))
                .param("id2", uuidToBytes(book2.getId()))
                .query(Integer.class)
                .single();

        assertEquals(2, count);
    }

    @Test
    void updateBook() throws Exception {
        Book book = new Book(
                bookId,
                "Old Title",
                "Old description",
                "old.jpg",
                BookStatus.AVAILABLE,
                user,
                author,
                genre
        );

        bookRepository.save(book);

        BookRequest request = new BookRequest(
                bookId,
                "New Title",
                "New description",
                "new.jpg",
                ownerId,
                authorId,
                genreId
        );

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.photoURL").value("new.jpg"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        String savedTitle = jdbcClient.sql(
                        "SELECT title FROM books WHERE id = :id")
                .param("id", uuidToBytes(bookId))
                .query(String.class)
                .single();

        assertEquals("New Title", savedTitle);

        String savedDescription = jdbcClient.sql(
                        "SELECT description FROM books WHERE id = :id")
                .param("id", uuidToBytes(bookId))
                .query(String.class)
                .single();

        assertEquals("New description", savedDescription);

        String savedPhotoUrl = jdbcClient.sql(
                        "SELECT photoURL FROM books WHERE id = :id")
                .param("id", uuidToBytes(bookId))
                .query(String.class)
                .single();

        assertEquals("new.jpg", savedPhotoUrl);
    }

    @Test
    void deleteBook() throws Exception {
        Book book = new Book(
                bookId,
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.AVAILABLE,
                user,
                author,
                genre
        );

        bookRepository.save(book);

        Integer countBefore = jdbcClient.sql(
                        "SELECT COUNT(*) FROM books WHERE id = :id")
                .param("id", uuidToBytes(bookId))
                .query(Integer.class)
                .single();

        assertEquals(1, countBefore);

        mockMvc.perform(delete("/api/v1/books/{id}", bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        Integer countAfter = jdbcClient.sql(
                        "SELECT COUNT(*) FROM books WHERE id = :id")
                .param("id", uuidToBytes(bookId))
                .query(Integer.class)
                .single();

        assertEquals(0, countAfter);
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/books/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnProblemDetailWithValidationError() throws Exception {
        BookRequest invalidRequest = new BookRequest(
                null,
                "",
                "Description",
                "photo.jpg",
                ownerId,
                authorId,
                genreId
        );

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        String invalidJson = """
                {
                    "title": "Test Book",
                    "description":
                }
                """;

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid request body"));
    }
}