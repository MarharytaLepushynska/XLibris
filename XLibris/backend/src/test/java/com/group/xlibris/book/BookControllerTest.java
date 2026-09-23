package com.group.xlibris.book;

import tools.jackson.databind.ObjectMapper;
import com.group.xlibris.book.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    private UUID bookId;
    private UUID ownerId;
    private UUID authorId;
    private UUID genreId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        genreId = UUID.randomUUID();
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

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getBookById() throws Exception {

        Book book = new Book(
                bookId,
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.AVAILABLE,
                ownerId,
                authorId,
                genreId
        );

        bookRepository.save(book);

        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getAllBooks() throws Exception {

        Book book1 = new Book(
                UUID.randomUUID(),
                "Book One",
                "Description one",
                "photo1.jpg",
                BookStatus.AVAILABLE,
                ownerId,
                authorId,
                genreId
        );

        Book book2 = new Book(
                UUID.randomUUID(),
                "Book Two",
                "Description two",
                "photo2.jpg",
                BookStatus.BORROWED,
                ownerId,
                authorId,
                genreId
        );

        bookRepository.save(book1);
        bookRepository.save(book2);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateBook() throws Exception {

        Book book = new Book(
                bookId,
                "Old Title",
                "Old description",
                "old.jpg",
                BookStatus.AVAILABLE,
                ownerId,
                authorId,
                genreId
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void deleteBook() throws Exception {

        Book book = new Book(
                bookId,
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.AVAILABLE,
                ownerId,
                authorId,
                genreId
        );

        bookRepository.save(book);

        mockMvc.perform(delete("/api/v1/books/{id}", bookId))
                .andExpect(status().isNoContent());
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }
}