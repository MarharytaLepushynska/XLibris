package com.group.xlibris.user.dto;

import com.group.xlibris.user.entity.User;
import com.group.xlibris.user.enums.Role;

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
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getCity(),
                user.getPhotoURL(),
                user.getRegistrationDate(),
                user.getRole(),
                user.getOwnerRating(),
                user.getBorrowerRating(),
                user.getSuccessfulOwnerLoans(),
                user.getSuccessfulBorrowerLoans(),
                user.getOverdueReturnsCount()
        );
    }
}
