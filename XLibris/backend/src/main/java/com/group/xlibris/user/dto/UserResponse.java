package com.group.xlibris.user.dto;

import com.group.xlibris.user.internal.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String city,
        String photoURL,
        Instant registrationDate,
        Role role,
        Double ownerRating,
        Double borrowerRating,
        int successfulOwnerLoans,
        int successfulBorrowerLoans,
        int overdueReturnsCount
) {
}
