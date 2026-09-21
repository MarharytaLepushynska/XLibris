package com.group.xlibris.user.dto;

import com.group.xlibris.user.command.UserUpdateAdminCommand;
import com.group.xlibris.user.enums.Role;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record AdminUserUpdateRequest(
        @NotNull
        UUID id,

        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 70)
        String city,

        String photoURL,

        @NotBlank
        @Size(max = 255)
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "^\\+380\\d{9}$")
        String phone,

        @NotNull
        Role role,

        @NotNull
        Double ownerRating,

        @NotNull
        Double borrowerRating,

        @NotNull
        Integer successfulOwnerLoans,

        @NotNull
        Integer successfulBorrowerLoans,

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
