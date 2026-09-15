package com.group.xlibris.bookWaitlist.dto;

import com.group.xlibris.bookWaitlist.enums.BookWaitlistStatus;
import jakarta.validation.constraints.NotNull;

public record BookWaitlistStatusUpdate(
        @NotNull
        BookWaitlistStatus staus
) {
}
