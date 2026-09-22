package com.group.xlibris.loan.service;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import com.group.xlibris.book.repository.BookRepository;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.loan.command.CreateLoanCommand;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.loan.enums.LoanStatus;
import com.group.xlibris.loan.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    public LoanServiceImpl(LoanRepository loanRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public LoanResponse getLoanById(UUID id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan (id = " + id + ") was not found"));
        return LoanResponse.from(loan);
    }

    @Override
    public List<LoanResponse> getAllLoans(UUID ownerId, UUID renterId, LoanStatus status) {
        List<Loan> loans = loanRepository.findByOwnerAndRenter(ownerId, renterId);

        return loans.stream()
                .filter(loan -> status == null || loan.getStatus() == status)
                .map(LoanResponse::from)
                .toList();
    }

    @Override
    public LoanResponse createLoan(CreateLoanCommand command) {
        Loan loan = Loan.create(command.bookId(), command.ownerId(), command.renterId(), command.expectedReturnDate());
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Override
    public LoanResponse returnLoan(UUID id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan (id = " + id + ") was not found"));
        Book book = bookRepository.findById(loan.getBookId())
                .orElseThrow(() -> new NotFoundException("Book was not found"));
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);
        loan.assignToReturned();
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Override
    public void removeLoan(UUID id) {
        if (!loanRepository.existsById(id)) {
            throw new NotFoundException("Loan (id = " + id + ") was not found");
        }
        loanRepository.deleteById(id);
    }
}
