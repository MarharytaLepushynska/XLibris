package com.group.xlibris.loan.exception;

import com.group.xlibris.common.exception.DomainException;

public class SameParticipantException extends DomainException {
    public SameParticipantException(String message) {
        super(message);
    }
}
