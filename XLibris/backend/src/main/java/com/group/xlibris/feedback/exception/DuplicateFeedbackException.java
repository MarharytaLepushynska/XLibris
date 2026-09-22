package com.group.xlibris.feedback.exception;

import com.group.xlibris.common.exception.DomainException;

public class DuplicateFeedbackException extends DomainException {

    public DuplicateFeedbackException(String message) {
        super(message);
    }
}