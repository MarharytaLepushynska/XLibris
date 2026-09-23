package com.group.xlibris.loan.exception;

import com.group.xlibris.common.DomainException;

public class SameParticipantException extends DomainException {
    public SameParticipantException(String message) {
        super(message);
    }
}
