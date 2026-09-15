package com.group.xlibris.author;

import com.group.xlibris.author.dto.AuthorRequest;
import com.group.xlibris.author.dto.AuthorResponse;
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
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAuthor() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                "J.R.R. Tolkien"
        );

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("J.R.R. Tolkien"));
    }

    @Test
    void getAuthorById() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                "J.R.R. Tolkien"
        );

        String response = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthorResponse createdAuthor =
                objectMapper.readValue(response, AuthorResponse.class);

        mockMvc.perform(get("/api/v1/authors/{id}", createdAuthor.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdAuthor.id().toString()))
                .andExpect(jsonPath("$.name").value("J.R.R. Tolkien"));
    }

    @Test
    void getAllAuthors() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                "J.R.R. Tolkien"
        );

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'J.R.R. Tolkien')]").exists());
    }

    @Test
    void updateAuthor() throws Exception {
        AuthorRequest createRequest = new AuthorRequest(
                null,
                "J.R.R. Tolkien"
        );

        String response = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthorResponse createdAuthor =
                objectMapper.readValue(response, AuthorResponse.class);

        AuthorRequest updateRequest = new AuthorRequest(
                createdAuthor.id(),
                "John Tolkien"
        );

        mockMvc.perform(put("/api/v1/authors/{id}", createdAuthor.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdAuthor.id().toString()))
                .andExpect(jsonPath("$.name").value("John Tolkien"));
    }

    @Test
    void deleteAuthor() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                "J.R.R. Tolkien"
        );

        String response = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthorResponse createdAuthor =
                objectMapper.readValue(response, AuthorResponse.class);

        mockMvc.perform(delete("/api/v1/authors/{id}", createdAuthor.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/authors/{id}", createdAuthor.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/authors/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnProblemDetailWithValidationError() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                ""
        );

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }
}