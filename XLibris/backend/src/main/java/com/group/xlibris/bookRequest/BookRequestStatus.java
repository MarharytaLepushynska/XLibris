package com.group.xlibris.bookRequest;

public enum BookRequestStatus {
    PENDING, APPROVED, FULFILLED, REJECTED, CANCELLED;

    public boolean isOpen() {
        return this == PENDING || this == APPROVED;
    }

    public boolean canTransitionTo(BookRequestStatus next) {
        return switch(this) {
            case PENDING -> next == APPROVED || next == REJECTED || next == CANCELLED;
            case APPROVED -> next == FULFILLED || next == REJECTED || next == CANCELLED;
            case FULFILLED, REJECTED, CANCELLED -> false;
        };
    }

}
