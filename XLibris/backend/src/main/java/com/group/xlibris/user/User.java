package com.group.xlibris.user;

import com.group.xlibris.user.internal.CreateUserCommand;
import com.group.xlibris.user.internal.UpdateUserCommand;
import com.group.xlibris.user.internal.UserUpdateAdminCommand;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User {
    protected User () {}

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 70)
    private String city;

    @Column
    private String photoURL;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, updatable = false)
    private Instant registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
