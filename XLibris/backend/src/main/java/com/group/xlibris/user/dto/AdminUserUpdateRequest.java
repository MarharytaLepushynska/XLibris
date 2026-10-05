package com.group.xlibris.user.dto;

import com.group.xlibris.user.internal.UserUpdateAdminCommand;
import com.group.xlibris.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Data for an administrator to update a user's details, role, and ratings")
public record AdminUserUpdateRequest(
        @Schema(description = "Unique id of the user", example = "550e8400-e29b-41d4-a716-446655446000")
        @NotNull
        UUID id,

        @Schema(description = "Full name of the user", example = "John Doe")
        @NotBlank
        @Size(max = 255)
        String name,

        @Schema(description = "City of residence", example = "Kyiv")
        @NotBlank
        @Size(max = 70)
        String city,

        @Schema(description = "URL to the user's profile photo", example = "https://example.com/photos/john.jpg")
        String photoURL,

        @Schema(description = "Email address of the user", example = "john.doe@example.com")
        @NotBlank
        @Size(max = 255)
        @Email
        String email,

        @Schema(description = "Phone number in Ukrainian format", example = "+380501234567")
        @NotBlank
        @Pattern(regexp = "^\\+380\\d{9}$")
        String phone,

        @Schema(description = "System role assigned to the user", example = "USER")
        @NotNull
        Role role,

        @Schema(description = "Owner reputation rating", example = "4.8")
        @NotNull
        Double ownerRating,

        @Schema(description = "Borrower reputation rating", example = "5.0")
        @NotNull
        Double borrowerRating,

        @Schema(description = "Number of successful owner loans", example = "12")
        @NotNull
        Integer successfulOwnerLoans,

        @Schema(description = "Number of successful borrower loans", example = "7")
        @NotNull
        Integer successfulBorrowerLoans,

        @Schema(description = "Total count of overdue returns", example = "0")
        @NotNull
        Integer overdueReturnsCount
) {
        public UserUpdateAdminCommand toCommand() {
                return new UserUpdateAdminCommand(
                        this.id,
                        this.name,
                        this.city,
                        this.photoURL,
                        this.email,
                        this.phone,
                        this.role,
                        this.ownerRating,
                        this.borrowerRating,
                        this.successfulOwnerLoans,
                        this.successfulBorrowerLoans,
                        this.overdueReturnsCount
                );
        }
}
