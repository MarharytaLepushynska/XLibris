package com.group.xlibris.feedback;

import com.group.xlibris.common.DomainException;

public class DuplicateFeedbackException extends DomainException {

    public DuplicateFeedbackException(String message) {
        super(message);
    }
}