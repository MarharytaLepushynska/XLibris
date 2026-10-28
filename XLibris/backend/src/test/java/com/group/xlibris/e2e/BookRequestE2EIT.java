package com.group.xlibris.e2e;

import com.group.xlibris.author.Author;
import com.group.xlibris.author.AuthorRepository;
import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.bookRequest.dto.BookRequestCreate;
import com.group.xlibris.bookRequest.dto.BookRequestUpdateStatus;
import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.security.dto.AuthResponse;
import com.group.xlibris.security.dto.LoginRequest;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.internal.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@E2ETest
class BookRequestE2EIT {

    @Autowired
    RestTestClient client;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    GenreRepository genreRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void requesterCanRequestBookAndOwnerCanApproveIt() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        String password = "TestPassword123!";

        User owner = new User(
                ownerId, "Book Owner", "Kyiv", null,
                "owner-" + ownerId + "@example.com",
                "+380991111111", Instant.now(), Role.USER,
                passwordEncoder.encode(password),
                0.0, 0.0, 0, 0, 0
        );

        User requester = new User(
                requesterId, "Book Requester", "Lviv", null,
                "requester-" + requesterId + "@example.com",
                "+380992222222", Instant.now(), Role.USER,
                passwordEncoder.encode(password),
                0.0, 0.0, 0, 0, 0
        );

        userRepository.save(owner);
        userRepository.save(requester);

        Author author = authorRepository.save(
                new Author(authorId, "E2E Test Author")
        );

        Genre genre = genreRepository.save(
                new Genre(genreId, "E2E Test Genre")
        );

        bookRepository.save(new Book(
                bookId,
                "E2E Test Book",
                "Book for the end-to-end test",
                null,
                BookStatus.AVAILABLE,
                owner,
                author,
                genre
        ));

        AuthResponse requesterAuth = login(
                requester.getEmail(), password
        );

        var createdRequest = client.post()
                .uri("/api/book-requests/{bookId}", bookId)
                .headers(h -> h.setBearerAuth(requesterAuth.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BookRequestCreate(requesterId, 14))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.bookId").isEqualTo(bookId.toString())
                .jsonPath("$.requesterId").isEqualTo(requesterId.toString())
                .jsonPath("$.ownerId").isEqualTo(ownerId.toString())
                .jsonPath("$.desiredDurationDays").isEqualTo(14)
                .jsonPath("$.status").isEqualTo("PENDING")
                .returnResult();

        String location = createdRequest.getResponseHeaders()
                .getFirst("Location");

        assertNotNull(location, "Creation response should contain Location");

        String requestId = location.substring(location.lastIndexOf('/') + 1);

        AuthResponse ownerAuth = login(owner.getEmail(), password);

        client.patch()
                .uri("/api/book-requests/{id}", requestId)
                .headers(h -> h.setBearerAuth(ownerAuth.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BookRequestUpdateStatus(
                        ownerId, BookRequestStatus.APPROVED
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(requestId)
                .jsonPath("$.status").isEqualTo("APPROVED");

        client.patch()
                .uri("/api/book-requests/{id}", requestId)
                .headers(h -> h.setBearerAuth(requesterAuth.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BookRequestUpdateStatus(
                        requesterId, BookRequestStatus.FULFILLED
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(requestId)
                .jsonPath("$.status").isEqualTo("FULFILLED");




        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<LoanResponse> loans = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/loans")
                            .queryParam("renterId", requesterId)
                            .build())
                    .headers(h -> h.setBearerAuth(requesterAuth.accessToken()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LoanResponse>>() {})
                    .returnResult()
                    .getResponseBody();

            assertNotNull(loans);
            assertEquals(1, loans.size());

            LoanResponse loan = loans.get(0);
            assertNotNull(loan.id());
            assertEquals(bookId, loan.bookId());
            assertEquals(ownerId, loan.ownerId());
            assertEquals(requesterId, loan.renterId());
            assertNotNull(loan.status());
        });
    }

    private AuthResponse login(String email, String password) {
        AuthResponse response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response, "Login response should not be null");
        assertNotNull(response.accessToken(), "Access token should not be null");

        return response;
    }
}
