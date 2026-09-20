package com.group.xlibris.report.exception;

import com.group.xlibris.common.exception.DomainException;

public class NotLoanParticipantException extends DomainException {
    public NotLoanParticipantException(String message) {
        super(message);
    }
}
