package com.group.xlibris.book;

import java.util.UUID;

public record BookBlockedEvent(
        UUID bookId
) {}
