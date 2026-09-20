package com.group.xlibris.report.exception;

import com.group.xlibris.common.exception.DomainException;

public class InvalidReportResolutionException extends DomainException {
    public InvalidReportResolutionException(String message) {
        super(message);
    }
}
