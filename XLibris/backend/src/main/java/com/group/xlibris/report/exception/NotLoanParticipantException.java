package com.group.xlibris.report.exception;

import com.group.xlibris.common.DomainException;

public class NotLoanParticipantException extends DomainException {
    public NotLoanParticipantException(String message) {
        super(message);
    }
}
