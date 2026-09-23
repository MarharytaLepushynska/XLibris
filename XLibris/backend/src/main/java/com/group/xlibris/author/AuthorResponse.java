package com.group.xlibris.author;

import java.util.UUID;

public record AuthorResponse(
        UUID id,
        String name
) {
}
