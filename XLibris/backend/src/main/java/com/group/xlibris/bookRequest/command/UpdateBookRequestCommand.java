package com.group.xlibris.bookRequest.command;

import com.group.xlibris.landCommon.BookRequestStatus;

import java.util.UUID;

public record UpdateBookRequestCommand(
        UUID requestId,
        UUID actorId,
        BookRequestStatus targetStatus
) {}
