package com.group.xlibris.bookRequest.exception;

import com.group.xlibris.common.exception.DomainException;

public class InvalidBookRequestStateException extends DomainException {
    public InvalidBookRequestStateException(String message) {
        super(message);
    }
}
