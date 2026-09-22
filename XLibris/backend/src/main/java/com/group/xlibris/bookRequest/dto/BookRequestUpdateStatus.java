package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.command.UpdateBookRequestCommand;
import com.group.xlibris.bookRequest.enums.BookRequestStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookRequestUpdateStatus(
        @NotNull
        BookRequestStatus status
) {
        public UpdateBookRequestCommand toCommand(UUID requestId) {
                return new UpdateBookRequestCommand(
                        requestId,
                        this.status
                );
        }
}
