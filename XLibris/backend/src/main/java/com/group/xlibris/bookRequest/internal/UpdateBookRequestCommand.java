package com.group.xlibris.bookRequest.internal;

import com.group.xlibris.bookRequest.BookRequestStatus;

import java.util.UUID;

public record UpdateBookRequestCommand(
        UUID requestId,
        UUID actorId,
        BookRequestStatus targetStatus
) {}
