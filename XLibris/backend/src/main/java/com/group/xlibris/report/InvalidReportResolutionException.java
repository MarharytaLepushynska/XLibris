package com.group.xlibris.report;

import com.group.xlibris.common.DomainException;

public class InvalidReportResolutionException extends DomainException {
    public InvalidReportResolutionException(String message) {
        super(message);
    }
}
