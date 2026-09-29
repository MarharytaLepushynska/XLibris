package com.group.xlibris.loan.internal;

import com.group.xlibris.loan.LoanStatus;
import com.group.xlibris.user.UserLoanCheck;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoanUserCheck implements UserLoanCheck {
    private final LoanRepository loanRepository;

    public LoanUserCheck(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public boolean canViewContacts(UUID targetUserId, UUID viewerId) {
       return !loanRepository.findLoansByCriteria(targetUserId, viewerId).isEmpty();
    }

    @Override
    public boolean hasActiveLoans(UUID userId) {
        return (loanRepository.findLoansByCriteria(userId, null)
                .stream()
                .anyMatch(l -> l.getStatus() != LoanStatus.RETURNED) ||
                loanRepository.findLoansByCriteria(null, userId)
                        .stream()
                        .anyMatch(l -> l.getStatus() != LoanStatus.RETURNED));
    }
}
