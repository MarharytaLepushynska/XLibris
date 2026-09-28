package com.group.xlibris.user;

import com.group.xlibris.common.DomainException;

public class UserHasActiveLoansException extends DomainException {
    public UserHasActiveLoansException(String message) {
        super(message);
    }
}
