package com.group.xlibris.loan.controller;

import com.group.xlibris.author.Author;
import com.group.xlibris.author.AuthorRepository;
import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.loan.dto.LoanRequest;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.loan.internal.LoanRepository;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.internal.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoanControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    private final UUID activeLoanId = UUID.fromString("550e8400-e29b-41d4-a716-446655444000");
    private final UUID overdueLoanId = UUID.fromString("550e8400-e29b-41d4-a716-446655444001");

    private final UUID bookId = UUID.fromString("550e8400-e29b-41d4-a716-446655445000");
    private final UUID bookId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655445001");

    private final UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655446000");
    private final UUID ownerId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655446001");

    private final UUID renterId = UUID.fromString("550e8400-e29b-41d4-a716-446655447000");
    private final UUID renterId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655447001");

    private final UUID authorId = UUID.fromString("550e8400-e29b-41d4-a716-446655448001");
    private final UUID genreId = UUID.fromString("550e8400-e29b-41d4-a716-446655448001");

    private final UUID newBookId = UUID.fromString("550e8400-e29b-41d4-a716-446655445002");
    private final UUID newOwnerId = UUID.fromString("550e8400-e29b-41d4-a716-446655446002");
    private final UUID newRenterId = UUID.fromString("550e8400-e29b-41d4-a716-446655447002");

    @BeforeEach
    void resetMap() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        User user = new User(ownerId, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);

        User user2 = new User(ownerId2, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);
        User renter = new User(renterId, "Ivan", "Rivne", null, "i@gmail.com",
                "+380111111111", Instant.now(), Role.USER, 0.0, 1.9, 4, 5, 1);

        User renter2 = new User(renterId2, "Oleg", "Odesa", null, "o@gmail.com",
                "+380222222222", Instant.now(), Role.USER, 2.0, 1.9, 4, 5, 1);

        User newOwner = new User(newOwnerId, "Anna", "Lviv", null, "a@gmail.com",
                "+380333333333", Instant.now(), Role.USER, 1.0, 1.0, 0, 0, 0);

        User newRenter = new User(newRenterId, "Petro", "Kharkiv", null, "p@gmail.com",
                "+380444444444", Instant.now(), Role.USER, 1.0, 1.0, 0, 0, 0);

        userRepository.save(user);
        userRepository.save(user2);
        userRepository.save(renter);
        userRepository.save(renter2);
        userRepository.save(newOwner);
        userRepository.save(newRenter);

        Author author = new Author(authorId, "JK Rowling");
        Genre genre = new Genre(genreId, "Horror");

        authorRepository.save(author);
        genreRepository.save(genre);

        Book book = new Book(bookId, "Test Book 1", "desc", null, BookStatus.BORROWED, user, author, genre);
        Book book2 = new Book(bookId2, "Test Book 2", "desc", null, BookStatus.BORROWED, user2, author, genre);
        Book newBook = new Book(newBookId, "Test Book 3", "desc", null, BookStatus.AVAILABLE, newOwner, author, genre);

        bookRepository.save(book);
        bookRepository.save(book2);
        bookRepository.save(newBook);

        Loan loan = new Loan(activeLoanId, book, user, renter, Instant.now(), Instant.now().plusSeconds(86400 * 14), null);
        Loan loan2 = new Loan(overdueLoanId, book2, user2, renter2, Instant.now().minusSeconds(86400 * 14), Instant.now(), null);

        loanRepository.save(loan);
        loanRepository.save(loan2);
    }

    @Test
    void shouldCreateLoan() throws Exception {
        LoanRequest request = new LoanRequest(
                newBookId,
                newOwnerId,
                newRenterId,
                Instant.now().plusSeconds(86400 * 14)
        );

        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void shouldGetLoanById() throws Exception {
        mvc.perform(get("/api/loans/{id}", activeLoanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.renterId").value(renterId.toString()));
    }

    @Test
    void shouldGetAllLoans() throws Exception {
        mvc.perform(get("/api/loans"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

    }

    @Test
    void shouldGetAllOverdueLoans() throws Exception {
        mvc.perform(get("/api/loans/overdue"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFilterLoansByStatus() throws Exception {
        mvc.perform(get("/api/loans")
                        .param("loanStatus", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldFilterLoansByOwnerId() throws Exception {
        mvc.perform(get("/api/loans")
                        .param("ownerId", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerId").value(ownerId.toString()));
    }

    @Test
    void shouldFilterLoansByRenterId() throws Exception {
        mvc.perform(get("/api/loans")
                        .param("renterId", renterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].renterId").value(renterId.toString()));
    }

    @Test
    void shouldAssignToReturned() throws Exception {
        mvc.perform(patch("/api/loans/{id}/return", activeLoanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeLoanId.toString()))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.actualReturnDate").exists());
    }

    @Test
    void shouldDeleteLoan() throws Exception {
        mvc.perform(delete("/api/loans/{id}", overdueLoanId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ Invalid body }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }

    @Test
    void shouldReturnValidationProblemDetail() throws Exception {
        LoanRequest invalidRequest = new LoanRequest(
                null,
                ownerId,
                renterId,
                null
        );

        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.bookId").exists())
                .andExpect(jsonPath("$.errors.expectedReturnDate").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentLoan() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655444003");
        mvc.perform(get("/api/loans/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentLoan() throws Exception {
        UUID nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655444003");

        mvc.perform(delete("/api/loans/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnConflictWhenAssigningAlreadyReturnedLoan() throws Exception {
        mvc.perform(patch("/api/loans/{id}/return", activeLoanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mvc.perform(patch("/api/loans/{id}/return", activeLoanId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Invalid loan state"))
                .andExpect(jsonPath("$.detail").value("Only not returned loans can be returned"));
    }

    @Test
    void shouldRejectUnknownPropertiesInJson() throws Exception {
        String jsonWithUnknownField = """
                {
                    "bookId": "550e8400-e29b-41d4-a716-446655445002",
                    "ownerId": "550e8400-e29b-41d4-a716-446655446002",
                    "renterId": "550e8400-e29b-41d4-a716-446655447002",
                    "expectedReturnDate": "2030-01-01T00:00:00Z",
                    "unexpectedField": "suspiciousValue"
                }
                """;

        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithUnknownField))
                .andExpect(status().isBadRequest());
    }
}
