package com.group.xlibris.loan.internal;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.loan.LoanCreatedEvent;
import com.group.xlibris.loan.LoanReturnedEvent;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.LoanStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LoanServiceImpl(LoanRepository loanRepository, ApplicationEventPublisher eventPublisher) {
        this.loanRepository = loanRepository;
        this.eventPublisher = eventPublisher;
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
        Loan saved = loanRepository.save(loan);

        eventPublisher.publishEvent(new LoanCreatedEvent(
                saved.getId(),
                saved.getBookId(),
                saved.getOwnerId(),
                saved.getRenterId(),
                saved.getStartDate(),
                saved.getExpectedReturnDate()
        ));

        return LoanResponse.from(saved);
    }

    @Override
    public LoanResponse returnLoan(UUID id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan (id = " + id + ") was not found"));
        loan.assignToReturned();

        Loan saved = loanRepository.save(loan);

        eventPublisher.publishEvent(new LoanReturnedEvent(
                saved.getId(),
                saved.getBookId(),
                saved.getOwnerId(),
                saved.getRenterId(),
                saved.getActualReturnDate()
        ));

        return LoanResponse.from(saved);
    }

    @Override
    public void removeLoan(UUID id) {
        if (!loanRepository.existsById(id)) {
            throw new NotFoundException("Loan (id = " + id + ") was not found");
        }
        loanRepository.deleteById(id);
    }
}
