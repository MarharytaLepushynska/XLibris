package com.group.xlibris.feedback;

import com.group.xlibris.common.DomainException;

public class SelfFeedbackException extends DomainException {

    public SelfFeedbackException(String message) {
        super(message);
    }
}