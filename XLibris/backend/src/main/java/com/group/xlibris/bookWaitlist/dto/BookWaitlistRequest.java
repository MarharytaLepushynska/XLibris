package com.group.xlibris.bookWaitlist.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookWaitlistRequest(
        @NotNull
        UUID bookId,

        @NotNull
        UUID userId
) {}
