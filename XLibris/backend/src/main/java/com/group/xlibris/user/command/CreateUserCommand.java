package com.group.xlibris.user.command;

public record CreateUserCommand(
        String name,
        String city,
        String photoURL,
        String email,
        String phone
) {
}
