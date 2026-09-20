package com.group.xlibris.loan.exception;

import com.group.xlibris.common.exception.DomainException;

public class InvalidLoanStateException extends DomainException {
    public InvalidLoanStateException(String message) {
        super(message);
    }
}
