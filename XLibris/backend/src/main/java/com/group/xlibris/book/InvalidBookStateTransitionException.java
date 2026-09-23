package com.group.xlibris.book;

import com.group.xlibris.common.DomainException;

public class InvalidBookStateTransitionException extends DomainException {

    public InvalidBookStateTransitionException(String message) {
        super(message);
    }
}