package com.group.xlibris.loan;

import com.group.xlibris.loan.internal.CreateLoanCommand;
import com.group.xlibris.loan.dto.LoanResponse;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.UUID;

@NamedInterface(value = "api")
public interface LoanService {
    LoanResponse getLoanById(UUID id);
    List<LoanResponse> getAllLoans(UUID ownerId, UUID renterId, LoanStatus status);
    LoanResponse createLoan(CreateLoanCommand command);
    LoanResponse returnLoan(UUID id);
    void removeLoan(UUID id);
}
