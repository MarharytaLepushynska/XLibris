package com.group.xlibris.book;

import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.book.dto.BookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID ownerId;
    private UUID authorId;
    private UUID genreId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        genreId = UUID.randomUUID();
    }

    @Test
    void createBook() throws Exception {

        BookRequest request = new BookRequest(
                null,
                "The Hobbit",
                "A fantasy novel",
                "https://example.com/hobbit.jpg",
                "AVAILABLE",
                ownerId,
                authorId,
                genreId
        );

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Hobbit"))
                .andExpect(jsonPath("$.description").value("A fantasy novel"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.authorId").value(authorId.toString()))
                .andExpect(jsonPath("$.genreId").value(genreId.toString()));
    }

    @Test
    void getBookById() throws Exception {

        BookRequest request = new BookRequest(
                null,
                "The Hobbit",
                "A fantasy novel",
                "https://example.com/hobbit.jpg",
                "AVAILABLE",
                ownerId,
                authorId,
                genreId
        );

        String response = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookResponse createdBook =
                objectMapper.readValue(response, BookResponse.class);

        mockMvc.perform(get("/api/v1/books/{id}", createdBook.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdBook.id().toString()))
                .andExpect(jsonPath("$.title").value("The Hobbit"))
                .andExpect(jsonPath("$.authorId").value(authorId.toString()))
                .andExpect(jsonPath("$.genreId").value(genreId.toString()));
    }

    @Test
    void getAllBooks() throws Exception {

        BookRequest request = new BookRequest(
                null,
                "The Hobbit",
                "A fantasy novel",
                "https://example.com/hobbit.jpg",
                "AVAILABLE",
                ownerId,
                authorId,
                genreId
        );

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.title == 'The Hobbit')]").isNotEmpty());
    }

    @Test
    void updateBook() throws Exception {

        BookRequest createRequest = new BookRequest(
                null,
                "The Hobbit",
                "A fantasy novel",
                "https://example.com/hobbit.jpg",
                "AVAILABLE",
                ownerId,
                authorId,
                genreId
        );

        String response = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookResponse createdBook =
                objectMapper.readValue(response, BookResponse.class);

        BookRequest updateRequest = new BookRequest(
                createdBook.id(),
                "The Hobbit Updated",
                "Updated description",
                "https://example.com/new-hobbit.jpg",
                "BORROWED",
                ownerId,
                authorId,
                genreId
        );

        mockMvc.perform(put("/api/v1/books/{id}", createdBook.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdBook.id().toString()))
                .andExpect(jsonPath("$.title").value("The Hobbit Updated"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("BORROWED"));
    }

    @Test
    void deleteBook() throws Exception {

        BookRequest request = new BookRequest(
                null,
                "The Hobbit",
                "A fantasy novel",
                "https://example.com/hobbit.jpg",
                "AVAILABLE",
                ownerId,
                authorId,
                genreId
        );

        String response = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookResponse createdBook =
                objectMapper.readValue(response, BookResponse.class);

        mockMvc.perform(delete("/api/v1/books/{id}", createdBook.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/books/{id}", createdBook.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/books/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnProblemDetailWithValidationError() throws Exception {

        BookRequest request = new BookRequest(
                null,
                "",
                "A fantasy novel",
                "https://example.com/hobbit.jpg",
                "AVAILABLE",
                ownerId,
                authorId,
                genreId
        );

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }
}
