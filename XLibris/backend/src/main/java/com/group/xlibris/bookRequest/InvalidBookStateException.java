package com.group.xlibris.bookRequest;

import com.group.xlibris.common.DomainException;

public class InvalidBookStateException extends DomainException {
    public InvalidBookStateException(String message) {
        super(message);
    }
}
