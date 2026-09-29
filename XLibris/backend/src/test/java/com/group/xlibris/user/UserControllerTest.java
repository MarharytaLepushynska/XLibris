package com.group.xlibris.user;

import com.group.xlibris.author.Author;
import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.loan.internal.LoanRepository;
import com.group.xlibris.user.dto.AdminUserUpdateRequest;
import com.group.xlibris.user.dto.UserRequest;
import com.group.xlibris.user.internal.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private com.group.xlibris.author.AuthorRepository authorRepository;

    @Autowired
    private com.group.xlibris.genre.GenreRepository genreRepository;

    @BeforeEach
    void resetMap() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        authorRepository.deleteAll();
        genreRepository.deleteAll();

        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID id2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID adminId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();

        User entity = new User(id, "Artem", "Lviv", null, "a@gmail.com",
                "+380998876445", Instant.now(), Role.USER,
                1.0, 0.9, 0, 0, 0);

        User entity2 = new User(id2, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);

        User admin = new User(adminId, "Admin", "Kyiv", null, "a@gmail.com",
                "+380998876446", Instant.now(), Role.ADMIN,
                null, null, 0, 0, 0);

        userRepository.save(entity);
        userRepository.save(entity2);
        userRepository.save(admin);

        Author author = new Author(authorId, "Milan Kundera");
        authorRepository.save(author);

        Genre genre = new Genre(genreId, "Novel");
        genreRepository.save(genre);

        Book book = new Book(bookId, "Identity",
                "Identity (French: L'Identité) is a novel by Franco-Czech writer Milan Kundera, published in 1998",
                null, BookStatus.AVAILABLE, entity, author, genre);
        bookRepository.save(book);

        loanRepository.save(Loan.create(book, entity, entity2, Instant.now().plusSeconds(1_209_600)));
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserRequest request = new UserRequest(null, "Margo", "Kyiv",
                null, "ma@gmail.com", "+380661184556");

        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

    }

    @Test
    void shouldGetUserById() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Artem"))
                .andExpect(jsonPath("$.city").value("Lviv"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

    }

    @Test
    void shouldGetUserContactInfoById() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID id2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        mvc.perform(get("/api/users/{id}/contact", id).param("viewerId", id2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@gmail.com"))
                .andExpect(jsonPath("$.phone").value("+380998876445"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UserRequest request = new UserRequest(id, "Dmitro", "Berlin",
                null, "d@gmail.com", "+380998876445");

        mvc.perform(put("/api/users/{id}", id)
                        .param("requesterId", String.valueOf(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dmitro"))
                .andExpect(jsonPath("$.city").value("Berlin"));
    }

    @Test
    void shouldUpdateUserByAdmin() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID adminId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        AdminUserUpdateRequest request = new AdminUserUpdateRequest(id,
                "Ivan", "Toronto",null, "i@gmail.com",
                "+380997765338", Role.USER,
                1.3, 4.5, 4, 3, 2);

        mvc.perform(put("/api/users/{id}/admin", id)
                        .param("callerId", adminId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.city").value("Toronto"))
                .andExpect(jsonPath("$.ownerRating").value("1.3"))
                .andExpect(jsonPath("$.borrowerRating").value("4.5"))
                .andExpect(jsonPath("$.successfulOwnerLoans").value("4"))
                .andExpect(jsonPath("$.successfulBorrowerLoans").value("3"))
                .andExpect(jsonPath("$.overdueReturnsCount").value("2"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        loanRepository.deleteAll();

        mvc.perform(delete("/api/users/{id}", id)
                        .param("callerId", id.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnProblemDetailWithArgValidationError() throws Exception {
        UserRequest request = new UserRequest(null, "", "Kyiv",
                null, "ma@gmail.com", "+380661184556");

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ kevnw }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));

    }

    @Test
    void shouldReturnMismatchedIdProblemDetail() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UUID id2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UserRequest request = new UserRequest(id2, "Dmitro", "Berlin",
                null, "d@gmail.com", "+380998876445");

        mvc.perform(put("/api/users/{id}", id)
                        .param("requesterId", String.valueOf(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("ID mismatch"));
    }

    @Test
    void shouldReturnNotFoundEProblemDetail() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

}