package com.group.xlibris.report;

import com.group.xlibris.common.DomainException;

public class SelfReportException extends DomainException {
    public SelfReportException(String message) {
        super(message);
    }
}
