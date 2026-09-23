package com.group.xlibris.feedback.exception;

import com.group.xlibris.common.DomainException;

public class SelfFeedbackException extends DomainException {

    public SelfFeedbackException(String message) {
        super(message);
    }
}