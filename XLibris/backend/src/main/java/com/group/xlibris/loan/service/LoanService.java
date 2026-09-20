package com.group.xlibris.loan.service;

import com.group.xlibris.loan.command.CreateLoanCommand;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.enums.LoanStatus;
import java.util.List;
import java.util.UUID;

public interface LoanService {
    LoanResponse getLoanById(UUID id);
    List<LoanResponse> getAllLoans(UUID ownerId, UUID renterId, LoanStatus status);
    LoanResponse createLoan(CreateLoanCommand command);
    LoanResponse returnLoan(UUID id);
    void removeLoan(UUID id);
}
