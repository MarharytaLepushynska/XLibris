package com.group.xlibris.loan;

import com.group.xlibris.common.DomainException;

public class InvalidReturnDateException extends DomainException {
    public InvalidReturnDateException(String message) {
        super(message);
    }
}
