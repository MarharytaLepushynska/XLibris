package com.group.xlibris.genre;

import com.group.xlibris.genre.dto.GenreRequest;
import com.group.xlibris.genre.dto.GenreResponse;
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
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGenre() throws Exception {
        GenreRequest request = new GenreRequest(
                null,
                "Fantasy"
        );

        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Fantasy"));
    }

    @Test
    void getGenreById() throws Exception {
        GenreRequest request = new GenreRequest(
                null,
                "Fantasy"
        );

        String response = mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GenreResponse createdGenre =
                objectMapper.readValue(response, GenreResponse.class);

        mockMvc.perform(get("/api/v1/genres/{id}", createdGenre.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdGenre.id().toString()))
                .andExpect(jsonPath("$.name").value("Fantasy"));
    }

    @Test
    void getAllGenres() throws Exception {
        GenreRequest request = new GenreRequest(
                null,
                "Fantasy"
        );

        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'Fantasy')]").exists());
    }

    @Test
    void updateGenre() throws Exception {
        GenreRequest createRequest = new GenreRequest(
                null,
                "Fantasy"
        );

        String response = mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GenreResponse createdGenre =
                objectMapper.readValue(response, GenreResponse.class);

        GenreRequest updateRequest = new GenreRequest(
                createdGenre.id(),
                "Science Fiction"
        );

        mockMvc.perform(put("/api/v1/genres/{id}", createdGenre.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdGenre.id().toString()))
                .andExpect(jsonPath("$.name").value("Science Fiction"));
    }

    @Test
    void deleteGenre() throws Exception {
        GenreRequest request = new GenreRequest(
                null,
                "Fantasy"
        );

        String response = mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GenreResponse createdGenre =
                objectMapper.readValue(response, GenreResponse.class);

        mockMvc.perform(delete("/api/v1/genres/{id}", createdGenre.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/genres/{id}", createdGenre.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/genres/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnProblemDetailWithValidationError() throws Exception {
        GenreRequest request = new GenreRequest(
                null,
                ""
        );

        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }
}