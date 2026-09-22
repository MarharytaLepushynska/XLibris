package com.group.xlibris.bookRequest.enums;

public enum BookRequestStatus {
    PENDING, APPROVED, REJECTED, CANCELLED;

    public boolean canTransitionTo(BookRequestStatus next) {
        return switch(this) {
            case PENDING -> next == APPROVED || next == REJECTED || next == CANCELLED;
            case APPROVED, REJECTED, CANCELLED -> false;
        };
    }

}
