package com.group.xlibris.user.internal;

import java.util.UUID;

public record UpdateUserCommand(
        UUID id,
        String name,
        String city,
        String photoURL,
        String email,
        String phone
) {
}
