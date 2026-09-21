package com.group.xlibris.book.exception;

import com.group.xlibris.common.exception.DomainException;

public class InvalidBookStateTransitionException extends DomainException {

    public InvalidBookStateTransitionException(String message) {
        super(message);
    }
}