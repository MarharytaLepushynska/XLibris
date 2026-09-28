package com.group.xlibris.loan.internal;

import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.LoanStatus;
import com.group.xlibris.user.UserLoanCheck;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoanUserCheck implements UserLoanCheck {
    private final LoanService loanService;

    public LoanUserCheck(LoanService loanService) {
        this.loanService = loanService;
    }

    @Override
    public boolean canViewContacts(UUID targetUserId, UUID viewerId) {
       return !loanService.getAllLoans(targetUserId, viewerId, null).isEmpty();
    }

    @Override
    public boolean hasActiveLoans(UUID userId) {
        return (loanService.getAllLoans(userId, null, null)
                .stream()
                .anyMatch(l -> l.status() != LoanStatus.RETURNED) ||
                loanService.getAllLoans(null, userId, null)
                        .stream()
                        .anyMatch(l -> l.status() != LoanStatus.RETURNED));
    }
}
