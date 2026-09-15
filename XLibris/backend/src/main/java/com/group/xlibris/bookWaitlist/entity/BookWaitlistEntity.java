package com.group.xlibris.bookWaitlist.entity;

import com.group.xlibris.bookWaitlist.enums.BookWaitlistStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class BookWaitlistEntity {
    private UUID id;
    private UUID bookId;
    private UUID userId;
    private int position;
    private Instant joinedAt;
    private BookWaitlistStatus status;
    private Instant notifiedAt;
    private Instant responseDeadline;
}
