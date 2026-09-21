package com.group.xlibris.user.entity;

import com.group.xlibris.user.command.CreateUserCommand;
import com.group.xlibris.user.command.UpdateUserCommand;
import com.group.xlibris.user.command.UserUpdateAdminCommand;
import com.group.xlibris.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class User {
    private UUID id;
    private String name;
    private String city;
    private String photoURL;
    private String email;
    private String phone;
    private Instant registrationDate;

    private Role role;

    private Double ownerRating;
    private Double borrowerRating;
    private int successfulOwnerLoans;
    private int successfulBorrowerLoans;
    private int overdueReturnsCount;

    public static User create(CreateUserCommand command) {
        return new User (
                UUID.randomUUID(),
                command.name(),
                command.city(),
                command.photoURL(),
                command.email(),
                command.phone(),
                Instant.now(),
                Role.USER,
                0.0, 0.0, 0, 0, 0
        );
    }

    public void updateDetails(UpdateUserCommand command) {
        this.name = command.name();
        this.city = command.city();
        this.photoURL = command.photoURL();
        this.email = command.email();
        this.phone = command.phone();
    }

    public void updateDetails(UserUpdateAdminCommand command) {
        this.name = command.name();
        this.city = command.city();
        this.photoURL = command.photoURL();
        this.email = command.email();
        this.phone = command.phone();
        this.role = command.role();
        this.ownerRating = command.ownerRating();
        this.borrowerRating = command.borrowerRating();
        this.successfulOwnerLoans = command.successfulOwnerLoans();
        this.successfulBorrowerLoans = command.successfulBorrowerLoans();
        this.overdueReturnsCount = command.overdueReturnsCount();
    }

}
