package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.internal.CreateBookRequestCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookRequestCreate(
        @NotNull
        UUID requesterId,

        @Min(1)
        @Max(360)
        int desiredDurationDays
) {
        public CreateBookRequestCommand toCommand(UUID bookId) {
                return new CreateBookRequestCommand(
                        bookId,
                        this.requesterId,
                        this.desiredDurationDays
                );
        }
}
