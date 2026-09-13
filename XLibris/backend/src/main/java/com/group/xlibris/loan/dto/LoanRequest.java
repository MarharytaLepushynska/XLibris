package com.group.xlibris.loan.dto;

import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.Instant;
import java.util.UUID;

public record LoanRequest(
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @NotNull(groups = OnCreate.class)
        UUID bookId,

        @NotNull(groups = OnCreate.class)
        UUID ownerId,

        @NotNull(groups = OnCreate.class)
        UUID renterId,

        @NotNull(groups = OnCreate.class)
        @Future(groups = OnCreate.class)
        Instant expectedReturnDate,

        @Null(groups = OnCreate.class)
        Instant actualReturnDate
) {
}
