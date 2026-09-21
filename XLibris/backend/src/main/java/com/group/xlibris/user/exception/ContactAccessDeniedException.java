package com.group.xlibris.user.exception;

import com.group.xlibris.common.exception.DomainException;

public class ContactAccessDeniedException extends DomainException {
    public ContactAccessDeniedException(String message) {
        super(message);
    }
}
