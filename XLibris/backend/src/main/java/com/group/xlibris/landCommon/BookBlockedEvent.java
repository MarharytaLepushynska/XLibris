package com.group.xlibris.landCommon;

import java.util.UUID;

public record BookBlockedEvent(
        UUID bookId
) {}
