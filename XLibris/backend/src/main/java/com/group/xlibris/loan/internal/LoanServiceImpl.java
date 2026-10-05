package com.group.xlibris.loan.internal;

import com.group.xlibris.book.Book;
import com.group.xlibris.book.BookService;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.loan.LoanCreatedEvent;
import com.group.xlibris.loan.LoanReturnedEvent;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.LoanStatus;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;
    private final UserService userService;
    private final BookService bookService;
    private final ApplicationEventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(LoanServiceImpl.class);

    public LoanServiceImpl(LoanRepository loanRepository, UserService userService, BookService bookService, ApplicationEventPublisher eventPublisher) {
        this.loanRepository = loanRepository;
        this.userService = userService;
        this.bookService = bookService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LoanResponse getLoanById(UUID id) {

        log.debug("Finding loan by id={}", id);

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan (id = " + id + ") was not found"));
        return LoanResponse.from(loan);
    }

    @Override
    public Loan getLoanReferenceById(UUID id) {
        return loanRepository.getReferenceById(id);
    }

    @Override
    public List<LoanResponse> getAllLoans(UUID ownerId, UUID renterId, LoanStatus status) {

        log.debug("Fetching loans: ownerId={}, renterId={}, status={}", ownerId, renterId, status);

        List<Loan> loans = loanRepository.findLoansByCriteria(ownerId, renterId);

        return loans.stream()
                .filter(loan -> status == null || loan.getStatus() == status)
                .map(LoanResponse::from)
                .toList();
    }

    @Override
    public LoanResponse createLoan(CreateLoanCommand command) {
        Book bookProxy = bookService.getBookReferenceById(command.bookId());
        User ownerProxy = userService.getUserReferenceById(command.ownerId());
        User renterProxy = userService.getUserReferenceById(command.renterId());

        Loan loan = Loan.create(bookProxy, ownerProxy, renterProxy, command.expectedReturnDate());
        Loan saved = loanRepository.save(loan);
        log.info("Loan with id={} was created", saved.getId());

        eventPublisher.publishEvent(new LoanCreatedEvent(
                saved.getId(),
                saved.getBook().getId(),
                saved.getOwner().getId(),
                saved.getRenter().getId(),
                saved.getStartDate(),
                saved.getExpectedReturnDate()
        ));

        log.info("Event for loan creation was published");

        return LoanResponse.from(saved);
    }

    @Override
    public LoanResponse returnLoan(UUID id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan (id = " + id + ") was not found"));
        loan.assignToReturned();

        Loan saved = loanRepository.save(loan);

        log.info("Loan for book with id={} was marked as returned", saved.getBook().getId());

        eventPublisher.publishEvent(new LoanReturnedEvent(
                saved.getId(),
                saved.getBook().getId(),
                saved.getOwner().getId(),
                saved.getRenter().getId(),
                saved.getActualReturnDate()
        ));

        log.info("Event for loan return was published");

        return LoanResponse.from(saved);
    }

    @Override
    public void removeLoan(UUID id) {
        if (!loanRepository.existsById(id)) {
            throw new NotFoundException("Loan (id = " + id + ") was not found");
        }
        loanRepository.deleteById(id);

        log.info("Loan with id={} was deleted", id);
    }
}
