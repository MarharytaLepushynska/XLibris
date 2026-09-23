package com.group.xlibris.bookRequest.internal;

import java.util.UUID;

public record CreateBookRequestCommand(
        UUID bookId,
        UUID requesterId,
        int desiredDurationDays
) {}
