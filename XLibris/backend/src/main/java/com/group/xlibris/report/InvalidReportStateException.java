package com.group.xlibris.report;

import com.group.xlibris.common.DomainException;

public class InvalidReportStateException extends DomainException {
    public InvalidReportStateException(String message) {
        super(message);
    }
}
