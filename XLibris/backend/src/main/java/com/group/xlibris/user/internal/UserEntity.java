package com.group.xlibris.user.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserEntity {
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
}
