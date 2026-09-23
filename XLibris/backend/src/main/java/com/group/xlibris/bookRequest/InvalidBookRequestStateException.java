package com.group.xlibris.bookRequest;

import com.group.xlibris.common.DomainException;

public class InvalidBookRequestStateException extends DomainException {
    public InvalidBookRequestStateException(String message) {
        super(message);
    }
}
