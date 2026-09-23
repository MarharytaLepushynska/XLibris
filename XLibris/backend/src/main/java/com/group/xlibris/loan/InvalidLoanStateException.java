package com.group.xlibris.loan;

import com.group.xlibris.common.DomainException;

public class InvalidLoanStateException extends DomainException {
    public InvalidLoanStateException(String message) {
        super(message);
    }
}
