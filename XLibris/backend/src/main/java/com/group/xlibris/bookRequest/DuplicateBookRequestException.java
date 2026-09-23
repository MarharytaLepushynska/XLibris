package com.group.xlibris.bookRequest;

import com.group.xlibris.common.DomainException;

public class DuplicateBookRequestException extends DomainException {
    public DuplicateBookRequestException(String message) {
        super(message);
    }
}
