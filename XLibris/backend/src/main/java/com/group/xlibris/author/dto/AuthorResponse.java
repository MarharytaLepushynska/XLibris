package com.group.xlibris.author.dto;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed representation of an author response")
public record AuthorResponse(

        @Schema(description = "Unique id of the author", example = "550e8400-e29b-41d4-a716-446655448001")
        UUID id,

        @Schema(description = "Full name of the author", example = "J.K. Rowling")
        String name
) {
}
