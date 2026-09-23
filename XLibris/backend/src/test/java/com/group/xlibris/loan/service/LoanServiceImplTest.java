package com.group.xlibris.loan.service;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import com.group.xlibris.book.repository.BookRepository;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.loan.command.CreateLoanCommand;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.loan.enums.LoanStatus;
import com.group.xlibris.loan.exception.InvalidLoanStateException;
import com.group.xlibris.loan.exception.InvalidReturnDateException;
import com.group.xlibris.loan.exception.SameParticipantException;
import com.group.xlibris.loan.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private BookRepository bookRepository;

    private LoanServiceImpl loanService;

    private UUID loanId;
    private UUID bookId;
    private UUID ownerId;
    private UUID renterId;
    private Instant expectedReturnDate;
    private Loan loan;

    @BeforeEach
    void setUp() {
        loanService = new LoanServiceImpl(loanRepository, bookRepository);

        loanId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        expectedReturnDate = Instant.now().plusSeconds(86400 * 14);

        loan = new Loan(
                loanId,
                bookId,
                ownerId,
                renterId,
                Instant.now(),
                expectedReturnDate,
                null
        );
    }

    @Test
    void shouldGetByIdSuccessfully() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        LoanResponse response = loanService.getLoanById(loanId);

        assertNotNull(response);
        assertEquals(loanId, response.id());
        assertEquals(bookId, response.bookId());
        assertEquals(ownerId, response.ownerId());
        assertEquals(renterId, response.renterId());
        assertEquals(LoanStatus.ACTIVE, response.status());
        assertNull(response.actualReturnDate());

        verify(loanRepository).findById(loanId);
    }

    @Test
    void shouldThrowNotFoundWhenLoanMissing() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loanService.getLoanById(loanId));
        verify(loanRepository).findById(loanId);
    }

    @Test
    void shouldGetAllLoansSuccessfully() {
        UUID loanId2 = UUID.randomUUID();
        Loan overdueLoan = new Loan(
                loanId2,
                UUID.randomUUID(),
                ownerId,
                renterId,
                Instant.now().minusSeconds(86400 * 10),
                Instant.now().minusSeconds(1),
                null
        );

        when(loanRepository.findByOwnerAndRenter(ownerId, renterId)).thenReturn(List.of(loan, overdueLoan));

        List<LoanResponse> responses = loanService.getAllLoans(ownerId, renterId, null);

        assertEquals(2, responses.size());
        verify(loanRepository).findByOwnerAndRenter(ownerId, renterId);
    }

    @Test
    void shouldFilterLoansByStatusSuccessfully() {
        UUID loanId2 = UUID.randomUUID();
        Loan overdueLoan = new Loan(
                loanId2,
                UUID.randomUUID(),
                ownerId,
                renterId,
                Instant.now().minusSeconds(86400 * 10),
                Instant.now().minusSeconds(1),
                null
        );

        when(loanRepository.findByOwnerAndRenter(ownerId, renterId)).thenReturn(List.of(loan, overdueLoan));

        List<LoanResponse> activeResponses = loanService.getAllLoans(ownerId, renterId, LoanStatus.ACTIVE);
        assertEquals(1, activeResponses.size());
        assertEquals(LoanStatus.ACTIVE, activeResponses.getFirst().status());

        List<LoanResponse> overdueResponses = loanService.getAllLoans(ownerId, renterId, LoanStatus.OVERDUE);
        assertEquals(1, overdueResponses.size());
        assertEquals(LoanStatus.OVERDUE, overdueResponses.getFirst().status());

        verify(loanRepository, times(2)).findByOwnerAndRenter(ownerId, renterId);
    }

    @Test
    void shouldCreateLoanSuccessfully() {
        CreateLoanCommand command = new CreateLoanCommand(bookId, ownerId, renterId, expectedReturnDate);

        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.createLoan(command);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(bookId, response.bookId());
        assertEquals(ownerId, response.ownerId());
        assertEquals(renterId, response.renterId());
        assertEquals(LoanStatus.ACTIVE, response.status());

        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void shouldThrowSameParticipantExceptionWhenOwnerIsRenter() {
        CreateLoanCommand command = new CreateLoanCommand(bookId, ownerId, ownerId, expectedReturnDate);

        assertThrows(SameParticipantException.class, () -> loanService.createLoan(command));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void shouldThrowInvalidReturnDateExceptionWhenDateIsInPast() {
        Instant pastDate = Instant.now().minusSeconds(3600);
        CreateLoanCommand command = new CreateLoanCommand(bookId, ownerId, renterId, pastDate);

        assertThrows(InvalidReturnDateException.class, () -> loanService.createLoan(command));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void shouldReturnLoanSuccessfully() {
        Book book = new Book(bookId, "HarryPotter", "some", null, BookStatus.BORROWED, ownerId, UUID.randomUUID(), UUID.randomUUID());

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.returnLoan(loanId);

        assertNotNull(response);
        assertEquals(LoanStatus.RETURNED, response.status());
        assertNotNull(response.actualReturnDate());

        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(loan);
        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    void shouldThrowNotFoundWhenReturningMissingLoan() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loanService.returnLoan(loanId));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void shouldThrowInvalidLoanStateExceptionWhenReturningAlreadyReturnedLoan() {
        loan.assignToReturned();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThrows(InvalidLoanStateException.class, () -> loanService.returnLoan(loanId));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
        verify(bookRepository, never()).findById(any());
    }

    @Test
    void shouldDeleteLoanSuccessfully() {
        when(loanRepository.existsById(loanId)).thenReturn(true);

        loanService.removeLoan(loanId);

        verify(loanRepository).existsById(loanId);
        verify(loanRepository).deleteById(loanId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentLoan() {
        when(loanRepository.existsById(loanId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> loanService.removeLoan(loanId));
        verify(loanRepository).existsById(loanId);
        verify(loanRepository, never()).deleteById(any());
    }
}