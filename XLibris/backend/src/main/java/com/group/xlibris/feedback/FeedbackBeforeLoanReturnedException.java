package com.group.xlibris.feedback;

import com.group.xlibris.common.DomainException;

public class FeedbackBeforeLoanReturnedException extends DomainException {

    public FeedbackBeforeLoanReturnedException(String message) {
        super(message);
    }
}