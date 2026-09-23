package com.group.xlibris.user;

import com.group.xlibris.common.DomainException;

public class ContactAccessDeniedException extends DomainException {
    public ContactAccessDeniedException(String message) {
        super(message);
    }
}
