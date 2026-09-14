package com.group.xlibris.genre.dto;

import java.util.UUID;

public record GenreResponse(
        UUID id,
        String name
) {
}