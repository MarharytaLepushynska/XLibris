package com.group.xlibris.integration;

import com.group.xlibris.IntegrationTest;
import com.group.xlibris.security.jwt.JwtService;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.internal.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
public class UserIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    private UUID createUser(Role role) {
        UUID id = UUID.randomUUID();

        User user = new User(id,
                "Test User",
                "Kyiv",
                null,
                id + "@test.local",
                "+380991234567",
                Instant.now(),
                role,
                passwordEncoder.encode("Password123!"),
                0.0, 0.0, 0, 0, 0);

        userRepository.saveAndFlush(user);
        return id;
    }

    private String generateToken(UUID userId) {
        String email = userId + "@test.local";
        var userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateToken(userDetails);
    }

    private String profileJson(UUID id, String name, String city) {
        return mapper.writeValueAsString(Map.of(
                "id", id,
                "name", name,
                "city", city,
                "email", id + "@test.local",
                "phone", "+380991234567"
        ));
    }

    private Map<String, Object> userRow(UUID id) {
        return jdbc.sql("""
                        SELECT name, city, email, phone, role, password
                        FROM users
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", id.toString())
                .query()
                .singleRow();
    }

    private long countUser(UUID id) {
        return jdbc.sql("""
                        SELECT COUNT(*)
                        FROM users
                        WHERE id = UUID_TO_BIN(:id)
                        """)
                .param("id", id.toString())
                .query(Long.class)
                .single();
    }

    @Test
    void createUser_verifyDatabase() throws Exception {
        String email = UUID.randomUUID() + "@test.local";
        String password = "Password123!";

        String response = mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "name", "New User",
                        "city", "Kyiv",
                        "email", email,
                        "phone", "+380991234567",
                        "password", password
                ))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID userId = UUID.fromString(
                mapper.readTree(response).get("id").asString()
        );
        Map<String, Object> row = userRow(userId);
        assertThat(row)
                .containsEntry("name", "New User")
                .containsEntry("city", "Kyiv")
                .containsEntry("email", email)
                .containsEntry("phone", "+380991234567")
                .containsEntry("role", "USER");

        String savedPassword = row.get("password").toString();
        assertThat(savedPassword).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, savedPassword))
                .isTrue();
    }

    @Test
    void updateOwnProfile_changesDatabaseRow() throws Exception {
        UUID userId = createUser(Role.USER);
        String token = generateToken(userId);

        mvc.perform(put("/api/users/{id}", userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson(
                                userId,
                                "Updated User",
                                "Lviv"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.city").value("Lviv"));

        assertThat(userRow(userId))
                .containsEntry("name", "Updated User")
                .containsEntry("city", "Lviv");
    }

    @Test
    void updateAnotherUser_returns403AndDoesNotChangeRow() throws Exception {
        UUID targetId = createUser(Role.USER);
        UUID actualId = createUser(Role.USER);
        String token = generateToken(actualId);

        Map<String, Object> before = userRow(targetId);

        mvc.perform(put("/api/users/{id}", targetId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson(
                                targetId,
                                "Updated User",
                                "Lviv"
                        )))
                .andExpect(status().isForbidden());

        assertThat(userRow(targetId)).isEqualTo(before);
    }

    @Test
    void deleteUser_asAdmin_removesDatabaseRow() throws Exception {
        UUID userId = createUser(Role.USER);
        UUID adminId = createUser(Role.ADMIN);
        String token = generateToken(adminId);

        assertThat(countUser(userId)).isEqualTo(1);

        mvc.perform(delete("/api/users/{id}", userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(countUser(userId)).isZero();

        mvc.perform(get("/api/users/{id}", userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"USER", "MODERATOR"}
    )
    void deleteUserAsNotAdmin_returns403DoesNotChangeRow(Role role) throws Exception {
        UUID targetId = createUser(Role.USER);
        UUID actualId = createUser(role);
        String token = generateToken(actualId);

        mvc.perform(delete("/api/users/{id}", targetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        assertThat(countUser(targetId)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"ADMIN", "MODERATOR"}
    )
    void getUsersAsModeratorAdmin_returnsUsers(Role role) throws Exception {
        UUID targetId = createUser(Role.USER);
        UUID actualId = createUser(role);
        String token = generateToken(actualId);
        String name = "User " + targetId;

        jdbc.sql("""
                    UPDATE users
                    SET name = :name
                    WHERE id = UUID_TO_BIN(:id)
                """)
                        .param("name", name)
                        .param("id", targetId.toString())
                        .update();

        mvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].id").value(targetId.toString())
                );

        assertThat(userRow(targetId))
                .containsEntry("name", name);
    }

    @Test
    void getUsersAsUser_returns403() throws Exception {
        UUID userId = createUser(Role.USER);
        String token = generateToken(userId);

        mvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUserWithInvalidData_returns400DoesNotCreateRow() throws Exception {
        String email = UUID.randomUUID() + "@test.local";

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "",
                                "city", "Kyiv",
                                "email", email,
                                "phone", "+380501234567",
                                "password", "short"
                        ))))
                .andExpect(status().isBadRequest());

        long count = jdbc.sql("""
                        SELECT COUNT(*)
                        FROM users
                        WHERE email = :email
                        """)
                .param("email", email)
                .query(Long.class)
                .single();

        assertThat(count).isZero();
    }

    @Test
    void getUserWithoutToken_returns401() throws Exception {
        UUID userId = createUser(Role.USER);

        mvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isUnauthorized());
    }
}
