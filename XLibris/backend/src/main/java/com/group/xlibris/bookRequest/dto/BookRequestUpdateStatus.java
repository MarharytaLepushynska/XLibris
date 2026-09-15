package com.group.xlibris.bookRequest.dto;

import com.group.xlibris.bookRequest.enums.BookRequestStatus;
import jakarta.validation.constraints.NotNull;

public record BookRequestUpdateStatus(
        @NotNull
        BookRequestStatus status
) {}
