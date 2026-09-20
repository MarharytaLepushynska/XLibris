package com.group.xlibris.report.exception;

import com.group.xlibris.common.exception.DomainException;

public class InvalidReportStateException extends DomainException {
    public InvalidReportStateException(String message) {
        super(message);
    }
}
