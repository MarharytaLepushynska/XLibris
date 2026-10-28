
package com.group.xlibris.author;

import com.group.xlibris.IntegrationTest;
import com.group.xlibris.author.dto.AuthorRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class AuthorControllerTest {

    private static final String TEST_PASSWORD = "TestPass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String email = "author-test-" + UUID.randomUUID() + "@example.com";

        User user = new User(
                UUID.randomUUID(),
                "Author Test User",
                "Kyiv",
                null,
                email,
                "+380000000000",
                Instant.now(),
                Role.USER,
                passwordEncoder.encode(TEST_PASSWORD),
                0.0,
                0.0,
                0,
                0,
                0
        );

        userRepository.save(user);

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

    @Test
    void createAuthor_ShouldPersistAuthorInDatabase() throws Exception {
        AuthorRequest request = new AuthorRequest(null, "Test Author");

        String response = mockMvc.perform(post("/api/v1/authors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Author"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID id = UUID.fromString(
                objectMapper.readTree(response).get("id").asText()
        );

        String name = jdbcClient.sql(
                        "SELECT name FROM authors WHERE id = :id")
                .param("id", uuidToBytes(id))
                .query(String.class)
                .single();

        assertEquals("Test Author", name);
    }

    @Test
    void getAuthorById() throws Exception {
        UUID id = createAuthorThroughApi("Test Author");

        mockMvc.perform(get("/api/v1/authors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Test Author"));
    }

    @Test
    void getAllAuthors() throws Exception {
        createAuthorThroughApi("Test Author");

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name")
                        .value(org.hamcrest.Matchers.hasItem("Test Author")));
    }

    @Test
    void updateAuthor_ShouldUpdateDatabaseRow() throws Exception {
        UUID id = createAuthorThroughApi("Test Author");
        AuthorRequest request = new AuthorRequest(id, "Updated Test Author");

        mockMvc.perform(put("/api/v1/authors/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Updated Test Author"));

        String name = jdbcClient.sql(
                        "SELECT name FROM authors WHERE id = :id")
                .param("id", uuidToBytes(id))
                .query(String.class)
                .single();

        assertEquals("Updated Test Author", name);
    }

    @Test
    void deleteAuthor_ShouldRemoveDatabaseRow() throws Exception {
        UUID id = createAuthorThroughApi("Test Author");

        mockMvc.perform(delete("/api/v1/authors/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        Long count = jdbcClient.sql(
                        "SELECT COUNT(*) FROM authors WHERE id = :id")
                .param("id", uuidToBytes(id))
                .query(Long.class)
                .single();

        assertEquals(0L, count);

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
        AuthorRequest request = new AuthorRequest(null, "");

        mockMvc.perform(post("/api/v1/authors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }

    private UUID createAuthorThroughApi(String name) throws Exception {
        AuthorRequest request = new AuthorRequest(null, name);

        String response = mockMvc.perform(post("/api/v1/authors")
                        .header("Authorization", "Bearer " + token)
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

    private byte[] uuidToBytes(UUID id) {
        return ByteBuffer.allocate(16)
                .putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits())
                .array();
    }
}