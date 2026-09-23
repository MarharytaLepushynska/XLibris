package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.command.UpdateBookRequestCommand;
import com.group.xlibris.landCommon.BookRequestStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookRequestUpdateStatus(
        @NotNull
        UUID actorId,

        @NotNull
        BookRequestStatus status
) {
        public UpdateBookRequestCommand toCommand(UUID requestId) {
                return new UpdateBookRequestCommand(
                        requestId,
                        actorId,
                        status
                );
        }
}
