package com.group.xlibris.feedback;

import com.group.xlibris.common.DomainException;

public class InvalidFeedbackParticipantsException extends DomainException {

    public InvalidFeedbackParticipantsException(String message) {
        super(message);
    }
}