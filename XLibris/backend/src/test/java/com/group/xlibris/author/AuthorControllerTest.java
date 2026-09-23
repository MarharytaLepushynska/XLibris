package com.group.xlibris.author;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                "Test Author"
        );

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Author"));
    }

    @Test
    void getAuthorById() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                "Test Author"
        );

        String response = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response)
                .get("id")
                .asText();

        mockMvc.perform(get("/api/v1/authors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Test Author"));
    }

    @Test
    void getAllAuthors() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                "Test Author"
        );

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name").value(
                        org.hamcrest.Matchers.hasItem("Test Author")
                ));
    }

    @Test
    void updateAuthor() throws Exception {
        AuthorRequest createRequest = new AuthorRequest(
                null,
                "Test Author"
        );

        String response = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID id = UUID.fromString(
                objectMapper.readTree(response).get("id").asText()
        );

        AuthorRequest updateRequest = new AuthorRequest(
                id,
                "Updated Test Author"
        );

        mockMvc.perform(put("/api/v1/authors/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Updated Test Author"));
    }

    @Test
    void deleteAuthor() throws Exception {
        AuthorRequest request = new AuthorRequest(
                null,
                "Test Author"
        );

        String response = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response)
                .get("id")
                .asText();

        mockMvc.perform(delete("/api/v1/authors/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/authors/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/authors/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail")
                        .value("Author with id " + id + " not found"));
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
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }
}