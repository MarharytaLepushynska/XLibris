package com.group.xlibris.bookRequest.entity;

import com.group.xlibris.landCommon.BookRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class BookRequestEntity {
    private UUID id;
    private UUID bookId;
    private UUID requesterId;
    private UUID ownerId;
    private int desiredDurationDays;
    private BookRequestStatus status;
    private Instant createdAt;
    private Instant respondedAt;
}
