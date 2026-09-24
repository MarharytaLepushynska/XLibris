package com.group.xlibris.author.dto;

import java.util.UUID;

public record AuthorResponse(
        UUID id,
        String name
) {
}
