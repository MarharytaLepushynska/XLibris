package com.group.xlibris.user;

import java.util.UUID;

public interface UserLoanCheck {
    boolean canViewContacts(UUID targetUserId, UUID viewerId);
    boolean hasActiveLoans(UUID userId);
}
