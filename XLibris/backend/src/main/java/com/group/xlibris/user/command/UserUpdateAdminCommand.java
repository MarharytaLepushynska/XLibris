package com.group.xlibris.user.command;

import com.group.xlibris.user.enums.Role;

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
