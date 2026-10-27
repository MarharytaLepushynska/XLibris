package com.group.xlibris.genre;

import com.group.xlibris.genre.dto.GenreRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void createGenre_ShouldPersistGenreInDatabase() throws Exception {
        GenreRequest request = new GenreRequest(null, "Fantasy");

        String response = mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Fantasy"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID id = UUID.fromString(
                objectMapper.readTree(response).get("id").asText()
        );

        String name = jdbcClient.sql(
                        "SELECT name FROM genres WHERE id = :id")
                .param("id", uuidToBytes(id))
                .query(String.class)
                .single();

        assertEquals("Fantasy", name);
    }

    @Test
    void getGenreById() throws Exception {
        UUID id = createGenreThroughApi("Fantasy");

        mockMvc.perform(get("/api/v1/genres/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Fantasy"));
    }

    @Test
    void getAllGenres() throws Exception {
        createGenreThroughApi("Fantasy");

        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name").value(
                        org.hamcrest.Matchers.hasItem("Fantasy")
                ));
    }

    @Test
    void updateGenre_ShouldUpdateDatabaseRow() throws Exception {
        UUID id = createGenreThroughApi("Fantasy");

        GenreRequest updateRequest =
                new GenreRequest(id, "Science Fiction");

        mockMvc.perform(put("/api/v1/genres/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Science Fiction"));

        String name = jdbcClient.sql(
                        "SELECT name FROM genres WHERE id = :id")
                .param("id", uuidToBytes(id))
                .query(String.class)
                .single();

        assertEquals("Science Fiction", name);
    }

    @Test
    void deleteGenre_ShouldRemoveDatabaseRow() throws Exception {
        UUID id = createGenreThroughApi("Fantasy");

        mockMvc.perform(delete("/api/v1/genres/{id}", id))
                .andExpect(status().isNoContent());

        Long count = jdbcClient.sql(
                        "SELECT COUNT(*) FROM genres WHERE id = :id")
                .param("id", uuidToBytes(id))
                .query(Long.class)
                .single();

        assertEquals(0L, count);

        mockMvc.perform(get("/api/v1/genres/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/genres/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title")
                        .value("Resource not found"))
                .andExpect(jsonPath("$.detail")
                        .value("Genre with id " + id + " not found"));
    }

    @Test
    void shouldReturnProblemDetailWithValidationError()
            throws Exception {
        GenreRequest request = new GenreRequest(null, "");

        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid request body"));
    }

    private byte[] uuidToBytes(UUID id) {
        return ByteBuffer.allocate(16)
                .putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits())
                .array();
    }

    private UUID createGenreThroughApi(String name) throws Exception {
        GenreRequest request = new GenreRequest(null, name);

        String response = mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(
                objectMapper.readTree(response).get("id").asText()
        );
    }
}