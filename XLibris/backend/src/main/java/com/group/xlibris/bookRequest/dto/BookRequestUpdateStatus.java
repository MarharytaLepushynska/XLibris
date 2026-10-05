package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.internal.UpdateBookRequestCommand;
import com.group.xlibris.common.BookRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Data to update the status of a book request")
public record BookRequestUpdateStatus(
        @Schema(description = "Unique id of the actor performing the status change", example = "550e8400-e29b-41d4-a716-446655446000")
        @NotNull
        UUID actorId,

        @Schema(description = "Target status to transition the book request to", example = "APPROVED")
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
