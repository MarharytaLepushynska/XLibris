package com.group.xlibris.book;

import org.springframework.modulith.NamedInterface;

@NamedInterface("api")
public enum BookStatus {
    AVAILABLE,
    BORROWED,
    BLOCKED
}
