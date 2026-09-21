package com.group.xlibris.user.exception;

import com.group.xlibris.common.exception.DomainException;

public class AccessDeniedException extends DomainException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
