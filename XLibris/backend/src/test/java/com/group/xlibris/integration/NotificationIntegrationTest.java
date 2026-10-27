package com.group.xlibris.integration;

import com.group.xlibris.IntegrationTest;
import com.group.xlibris.security.jwt.JwtService;
import com.group.xlibris.user.User;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.internal.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@IntegrationTest
@Transactional
class NotificationIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;


    private UUID insertUser() {

        UUID id = UUID.randomUUID();

        User user = new User(
                id,
                "Notification Test",
                "Kyiv",
                null,
                id + "@test.local",
                "000",
                Instant.now(),
                Role.USER,
                "test",
                0.0,
                0.0,
                0,
                0,
                0
        );

        userRepository.saveAndFlush(user);

        return id;
    }


    private String generateToken(UUID userId) {

        String email = userId + "@test.local";

        var userDetails =
                userDetailsService.loadUserByUsername(email);

        return jwtService.generateToken(userDetails);
    }


    @Test
    void createReadAndFilterNotification_verifyDatabase()
            throws Exception {

        UUID userId = insertUser();


        String token = generateToken(userId);


        String json = """
                {
                    "userId": "%s",
                    "type": "FEEDBACK_REQUESTED",
                    "message": "Please review"
                }
                """.formatted(userId);

        String response = mvc.perform(
                        post("/api/notifications")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();


        UUID id = UUID.fromString(
                mapper.readTree(response).get("id").asText()
        );
        entityManager.flush();

        String message = jdbc.sql("""
                SELECT message
                FROM notifications
                WHERE id = UUID_TO_BIN(:id)
                """)
                .param("id", id.toString())
                .query(String.class)
                .single();

        assertThat(message).isEqualTo("Please review");


        String savedUserId = jdbc.sql("""
                SELECT BIN_TO_UUID(user_id)
                FROM notifications
                WHERE id = UUID_TO_BIN(:id)
                """)
                .param("id", id.toString())
                .query(String.class)
                .single();

        assertThat(savedUserId)
                .isEqualToIgnoringCase(userId.toString());


        mvc.perform(
                        get("/api/notifications/{id}", id)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Please review"));


        mvc.perform(
                        get("/api/notifications")
                                .header("Authorization", "Bearer " + token)
                                .param("userId", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(id.toString()));
    }


    @Test
    void invalidNotification_isNotInserted() throws Exception {

        UUID userId = insertUser();

        String token = generateToken(userId);


        Long before = jdbc.sql("""
                SELECT COUNT(*)
                FROM notifications
                """)
                .query(Long.class)
                .single();


        String json = """
                {
                    "userId": "%s",
                    "type": "FEEDBACK_REQUESTED",
                    "message": ""
                }
                """.formatted(userId);


        mvc.perform(
                        post("/api/notifications")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());


        Long after = jdbc.sql("""
                SELECT COUNT(*)
                FROM notifications
                """)
                .query(Long.class)
                .single();

        assertThat(after).isEqualTo(before);
    }
}