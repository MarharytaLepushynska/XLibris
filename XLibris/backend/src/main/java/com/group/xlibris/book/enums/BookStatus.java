package com.group.xlibris.book.enums;

import org.springframework.modulith.NamedInterface;

@NamedInterface("api")
public enum BookStatus {
    AVAILABLE,
    BORROWED,
    BLOCKED
}
