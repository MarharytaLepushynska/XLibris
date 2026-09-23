package com.group.xlibris.user.internal;

import com.group.xlibris.user.Role;

import java.util.UUID;

public record UserUpdateAdminCommand(
        UUID id,
        String name,
        String city,
        String photoURL,
        String email,
        String phone,
        Role role,
        double ownerRating,
        double borrowerRating,
        int successfulOwnerLoans,
        int successfulBorrowerLoans,
        int overdueReturnsCount
) {
}
