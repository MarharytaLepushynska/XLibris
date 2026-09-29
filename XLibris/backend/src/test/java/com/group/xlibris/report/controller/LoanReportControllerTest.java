package com.group.xlibris.report.controller;

import com.group.xlibris.author.Author;
import com.group.xlibris.author.AuthorRepository;
import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.genre.GenreRepository;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.loan.internal.LoanRepository;
import com.group.xlibris.report.dto.LoanReportRequest;
import com.group.xlibris.report.internal.Report;
import com.group.xlibris.report.ReportType;
import com.group.xlibris.report.internal.ReportRepository;
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

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoanReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    private final UUID loanId = UUID.fromString("550e8400-e29b-41d4-a716-446655444000");
    private final UUID bookId = UUID.fromString("550e8400-e29b-41d4-a716-446655445000");
    private final UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655446000");
    private final UUID renterId = UUID.fromString("550e8400-e29b-41d4-a716-446655447000");
    private final UUID authorId = UUID.fromString("550e8400-e29b-41d4-a716-446655448001");
    private final UUID genreId = UUID.fromString("550e8400-e29b-41d4-a716-446655448002");

    @BeforeEach
    void resetMaps() {
        loanRepository.deleteAll();
        reportRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        User owner = new User(ownerId, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER, 2.0, 1.9, 4, 5, 1);

        User renter = new User(renterId, "Ivan", "Rivne", null, "i@gmail.com",
                "+380111111111", Instant.now(), Role.USER, 1.0, 1.0, 0, 0, 0);

        userRepository.save(owner);
        userRepository.save(renter);

        Author author = new Author(authorId, "JK Rowling");
        Genre genre = new Genre(genreId, "Horror");
        authorRepository.save(author);
        genreRepository.save(genre);

        Book book = new Book(bookId, "Test Book", "desc", null, BookStatus.BORROWED, owner, author, genre);
        bookRepository.save(book);

        Loan loan = new Loan(
                loanId,
                book,
                owner,
                renter,
                Instant.now(),
                Instant.now().plusSeconds(86400 * 14),
                null
        );
        loanRepository.save(loan);

        Report existingReport = Report.forLoan(
                loan,
                owner,
                renter,
                owner,
                "Book is damaged",
                ReportType.DAMAGED_BOOK,
                "Damaged pages by coffee",
                URI.create("https://example.com/proof.jpg")
        );
        reportRepository.save(existingReport);
    }

    @Test
    void shouldCreateReportForLoanByOwner() throws Exception {
        LoanReportRequest request = new LoanReportRequest(
                "Overdue of returning date",
                ReportType.OVERDUE_RETURN,
                "Book was returned after 2 months",
                URI.create("https://example.com/proof.jpg"),
                ownerId
        );

        mvc.perform(post("/api/loans/{loanId}/reports", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.loanId").value(loanId.toString()))
                .andExpect(jsonPath("$.reporterId").value(ownerId.toString()))
                .andExpect(jsonPath("$.targetUserId").value(renterId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("OVERDUE_RETURN"));
    }

    @Test
    void shouldCreateReportForLoanByRenter() throws Exception {
        LoanReportRequest request = new LoanReportRequest(
                "Owner of book was rude",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Owner was offensive for no reason",
                URI.create("https://example.com/proof.jpg"),
                renterId
        );

        mvc.perform(post("/api/loans/{loanId}/reports", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.reporterId").value(renterId.toString()))
                .andExpect(jsonPath("$.targetUserId").value(ownerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldGetAllReportsForLoan() throws Exception {
        mvc.perform(get("/api/loans/{loanId}/reports", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenReporterIsNotParticipantOfLoan() throws Exception {
        UUID thirdPartyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655449999");

        LoanReportRequest request = new LoanReportRequest(
                "Not participant of loan",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Offensive behavior",
                URI.create("https://example.com/proof.jpg"),
                thirdPartyUserId
        );

        mvc.perform(post("/api/loans/{loanId}/reports", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Not a loan participant"))
                .andExpect(jsonPath("$.detail").value("User " + thirdPartyUserId + " is not a participant of loan " + loanId));
    }

    @Test
    void shouldReturnNotFoundWhenLoanDoesNotExistOnCreate() throws Exception {
        UUID nonExistentLoanId = UUID.fromString("550e8400-e29b-41d4-a716-446655440099");

        LoanReportRequest request = new LoanReportRequest(
                "Damaged book",
                ReportType.DAMAGED_BOOK,
                "Book is damaged",
                URI.create("https://example.com/proof.jpg"),
                ownerId
        );

        mvc.perform(post("/api/loans/{loanId}/reports", nonExistentLoanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnNotFoundWhenLoanDoesNotExist() throws Exception {
        UUID nonExistentLoanId = UUID.fromString("550e8400-e29b-41d4-a716-446655440099");

        mvc.perform(get("/api/loans/{loanId}/reports", nonExistentLoanId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnValidationProblemDetail() throws Exception {
        LoanReportRequest invalidRequest = new LoanReportRequest(
                "",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Description",
                URI.create("https://example.com/proof.jpg"),
                null
        );

        mvc.perform(post("/api/loans/{loanId}/reports", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.reporterId").exists());
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mvc.perform(post("/api/loans/{loanId}/reports", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ Invalid body }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }

}