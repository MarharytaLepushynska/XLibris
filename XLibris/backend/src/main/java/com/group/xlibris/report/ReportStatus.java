package com.group.xlibris.report;

public enum ReportStatus {
    REJECTED,
    RESOLVED,
    PENDING,
    IN_REVIEW;

    public boolean canChangeStatus(ReportStatus toStatus) {
        return switch (this) {
            case PENDING -> toStatus == IN_REVIEW;
            case IN_REVIEW -> toStatus == RESOLVED || toStatus == REJECTED;
            case RESOLVED, REJECTED -> false;
        };
    }
}
