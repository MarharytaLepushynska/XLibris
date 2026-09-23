package com.group.xlibris.user.internal;

public record CreateUserCommand(
        String name,
        String city,
        String photoURL,
        String email,
        String phone
) {
}
