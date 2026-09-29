package com.group.xlibris.loan.service;


import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookService;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.loan.*;
import com.group.xlibris.loan.internal.CreateLoanCommand;

import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.loan.internal.LoanRepository;
import com.group.xlibris.loan.internal.LoanServiceImpl;
import com.group.xlibris.user.User;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private LoanServiceImpl loanService;

    private UUID loanId;
    private UUID bookId;
    private UUID ownerId;
    private UUID renterId;
    private Instant expectedReturnDate;
    private Loan loan;

    private Book book;
    private User owner;
    private User renter;

    @BeforeEach
    void setUp() {
        loanService = new LoanServiceImpl(loanRepository, userService, bookService, eventPublisher);

        loanId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        expectedReturnDate = Instant.now().plusSeconds(86400 * 14);

        book = mock(Book.class);
        when(book.getId()).thenReturn(bookId);

        owner = new User(ownerId, "Artem", "Lviv", null, "a@gmail.com",
                "+380998876443", Instant.now(), Role.USER, 1.0, 0.9, 0, 0, 0);

        renter = new User(renterId, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER, 2.0, 1.9, 4, 5, 1);

        loan = new Loan(
                loanId,
                book,
                owner,
                renter,
                Instant.now(),
                expectedReturnDate,
                null
        );
    }

    @Test
    void shouldGetByIdSuccessfully() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(book.getId()).thenReturn(bookId);

        LoanResponse response = loanService.getLoanById(loanId);

        assertNotNull(response);
        assertEquals(loanId, response.id());
        assertEquals(bookId, response.bookId());
        assertEquals(ownerId, response.ownerId());
        assertEquals(renterId, response.renterId());
        assertEquals(LoanStatus.ACTIVE, response.status());
        assertNull(response.actualReturnDate());

        verify(loanRepository).findById(loanId);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowNotFoundWhenLoanMissing() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loanService.getLoanById(loanId));
        verify(loanRepository).findById(loanId);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldGetAllLoansSuccessfully() {
        UUID loanId2 = UUID.randomUUID();
        Loan overdueLoan = new Loan(
                loanId2,
                book,
                owner,
                renter,
                Instant.now().minusSeconds(86400 * 10),
                Instant.now().minusSeconds(1),
                null
        );

        when(loanRepository.findLoansByCriteria(ownerId, renterId)).thenReturn(List.of(loan, overdueLoan));
        when(book.getId()).thenReturn(bookId);

        List<LoanResponse> responses = loanService.getAllLoans(ownerId, renterId, null);

        assertEquals(2, responses.size());
        verify(loanRepository).findLoansByCriteria(ownerId, renterId);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldFilterLoansByStatusSuccessfully() {
        UUID loanId2 = UUID.randomUUID();
        Loan overdueLoan = new Loan(
                loanId2,
                book,
                owner,
                renter,
                Instant.now().minusSeconds(86400 * 10),
                Instant.now().minusSeconds(1),
                null
        );

        when(loanRepository.findLoansByCriteria(ownerId, renterId)).thenReturn(List.of(loan, overdueLoan));
        when(book.getId()).thenReturn(bookId);

        List<LoanResponse> activeResponses = loanService.getAllLoans(ownerId, renterId, LoanStatus.ACTIVE);
        assertEquals(1, activeResponses.size());
        assertEquals(LoanStatus.ACTIVE, activeResponses.getFirst().status());

        List<LoanResponse> overdueResponses = loanService.getAllLoans(ownerId, renterId, LoanStatus.OVERDUE);
        assertEquals(1, overdueResponses.size());
        assertEquals(LoanStatus.OVERDUE, overdueResponses.getFirst().status());

        verify(loanRepository, times(2)).findLoansByCriteria(ownerId, renterId);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldCreateLoanSuccessfully() {
        CreateLoanCommand command = new CreateLoanCommand(bookId, ownerId, renterId, expectedReturnDate);

        when(bookService.getBookReferenceById(bookId)).thenReturn(book);
        when(userService.getUserReferenceById(ownerId)).thenReturn(owner);
        when(userService.getUserReferenceById(renterId)).thenReturn(renter);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(book.getId()).thenReturn(bookId);

        LoanResponse response = loanService.createLoan(command);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(bookId, response.bookId());
        assertEquals(ownerId, response.ownerId());
        assertEquals(renterId, response.renterId());
        assertEquals(LoanStatus.ACTIVE, response.status());

        verify(loanRepository).save(any(Loan.class));
        verify(eventPublisher).publishEvent(any(LoanCreatedEvent.class));
    }

    @Test
    void shouldThrowSameParticipantExceptionWhenOwnerIsRenter() {
        CreateLoanCommand command = new CreateLoanCommand(bookId, ownerId, ownerId, expectedReturnDate);

        when(bookService.getBookReferenceById(bookId)).thenReturn(book);
        when(userService.getUserReferenceById(ownerId)).thenReturn(owner);

        assertThrows(SameParticipantException.class, () -> loanService.createLoan(command));
        verify(loanRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowInvalidReturnDateExceptionWhenDateIsInPast() {
        Instant pastDate = Instant.now().minusSeconds(3600);
        CreateLoanCommand command = new CreateLoanCommand(bookId, ownerId, renterId, pastDate);

        when(bookService.getBookReferenceById(bookId)).thenReturn(book);
        when(userService.getUserReferenceById(ownerId)).thenReturn(owner);
        when(userService.getUserReferenceById(renterId)).thenReturn(renter);

        assertThrows(InvalidReturnDateException.class, () -> loanService.createLoan(command));
        verify(loanRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldReturnLoanSuccessfully() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(book.getId()).thenReturn(bookId);

        LoanResponse response = loanService.returnLoan(loanId);

        assertNotNull(response);
        assertEquals(LoanStatus.RETURNED, response.status());
        assertNotNull(response.actualReturnDate());

        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(loan);
        verify(eventPublisher).publishEvent(new LoanReturnedEvent(loanId, loan.getBook().getId(), loan.getOwner().getId(), loan.getRenter().getId(), loan.getActualReturnDate()));
        verify(eventPublisher).publishEvent(new LoanReturnedEvent(
                loan.getId(),
                loan.getBook().getId(),
                loan.getOwner().getId(),
                loan.getRenter().getId(),
                loan.getActualReturnDate()
        ));
    }

    @Test
    void shouldThrowNotFoundWhenReturningMissingLoan() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loanService.returnLoan(loanId));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowInvalidLoanStateExceptionWhenReturningAlreadyReturnedLoan() {
        loan.assignToReturned();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThrows(InvalidLoanStateException.class, () -> loanService.returnLoan(loanId));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldDeleteLoanSuccessfully() {
        when(loanRepository.existsById(loanId)).thenReturn(true);

        loanService.removeLoan(loanId);

        verify(loanRepository).existsById(loanId);
        verify(loanRepository).deleteById(loanId);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentLoan() {
        when(loanRepository.existsById(loanId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> loanService.removeLoan(loanId));
        verify(loanRepository).existsById(loanId);
        verify(loanRepository, never()).deleteById(any());
        verifyNoInteractions(eventPublisher);
    }
}