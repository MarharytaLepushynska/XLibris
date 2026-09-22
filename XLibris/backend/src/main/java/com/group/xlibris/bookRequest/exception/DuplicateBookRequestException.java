package com.group.xlibris.bookRequest.exception;

import com.group.xlibris.common.exception.DomainException;

public class DuplicateBookRequestException extends DomainException {
    public DuplicateBookRequestException(String message) {
        super(message);
    }
}
