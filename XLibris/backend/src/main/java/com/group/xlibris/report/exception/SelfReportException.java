package com.group.xlibris.report.exception;

import com.group.xlibris.common.exception.DomainException;

public class SelfReportException extends DomainException {
    public SelfReportException(String message) {
        super(message);
    }
}
