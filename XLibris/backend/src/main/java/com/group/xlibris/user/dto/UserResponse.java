package com.group.xlibris.user.dto;

import com.group.xlibris.user.User;
import com.group.xlibris.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Detailed representation of a user response profile")
public record UserResponse(
        @Schema(description = "Unique id of the user", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID id,

        @Schema(description = "Full name of the user", example = "John Doe")
        String name,

        @Schema(description = "City of residence", example = "Kyiv")
        String city,

        @Schema(description = "URL to the user's profile photo", example = "https://example.com/photos/john.jpg")
        String photoURL,

        @Schema(description = "Timestamp when the user registered", example = "2026-10-05T10:00:00Z")
        Instant registrationDate,

        @Schema(description = "System role of the user", example = "USER")
        Role role,

        @Schema(description = "Owner reputation rating", example = "4.8")
        Double ownerRating,

        @Schema(description = "Borrower reputation rating", example = "5.0")
        Double borrowerRating,

        @Schema(description = "Number of successful owner loans", example = "12")
        int successfulOwnerLoans,

        @Schema(description = "Number of successful borrower loans", example = "7")
        int successfulBorrowerLoans,

        @Schema(description = "Total count of overdue returns", example = "0")
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
