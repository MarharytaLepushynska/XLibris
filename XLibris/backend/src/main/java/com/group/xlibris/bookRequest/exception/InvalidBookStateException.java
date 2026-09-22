package com.group.xlibris.bookRequest.exception;

import com.group.xlibris.common.exception.DomainException;

public class InvalidBookStateException extends DomainException {
    public InvalidBookStateException(String message) {
        super(message);
    }
}
