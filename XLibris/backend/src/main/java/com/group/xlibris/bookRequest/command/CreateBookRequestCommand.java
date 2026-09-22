package com.group.xlibris.bookRequest.command;

import java.util.UUID;

public record CreateBookRequestCommand(
        UUID bookId,
        UUID requesterId,
        int desiredDurationDays
) {}
