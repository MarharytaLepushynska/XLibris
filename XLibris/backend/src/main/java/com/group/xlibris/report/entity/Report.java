package com.group.xlibris.report.entity;

import com.group.xlibris.report.enums.ReportAction;
import com.group.xlibris.report.enums.ReportStatus;
import com.group.xlibris.report.enums.ReportType;
import com.group.xlibris.report.exception.InvalidReportResolutionException;
import com.group.xlibris.report.exception.InvalidReportStateException;
import com.group.xlibris.report.exception.NotLoanParticipantException;
import com.group.xlibris.report.exception.SelfReportException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Report {
    private UUID id;
    private String title;
    private ReportType type;
    private String description;
    private URI evidenceUrl;
    private Instant createdAt;
    private UUID loanId;
    private UUID reporterId;
    private UUID targetUserId;
    private ReportStatus status;
    private String moderatorComment;
    private ReportAction moderatorVerdict;

    public static Report forLoan(UUID loanId, UUID ownerId,
                                 UUID renterId, UUID reporterId,
                                 String title, ReportType type,
                                 String description, URI evidenceUrl) {
        UUID targetUserId = resolveOpponent(loanId, ownerId, renterId, reporterId);
        return new Report(
                UUID.randomUUID(), title, type,
                description, evidenceUrl, Instant.now(),
                loanId, reporterId, targetUserId,
                ReportStatus.PENDING, null, null);
    }

    public static Report standalone(String title, ReportType type,
                                    String description, URI evidenceUrl,
                                    UUID reporterId, UUID targetUserId) {
        if (Objects.equals(reporterId, targetUserId)) {
            throw new SelfReportException("User cannot report themselves");
        }

        return new Report(
                UUID.randomUUID(), title, type,
                description, evidenceUrl, Instant.now(),
                null, reporterId, targetUserId,
                ReportStatus.PENDING, null, null);
    }

    private static UUID resolveOpponent(UUID loanId, UUID ownerId, UUID renterId, UUID reporterId) {
        if (Objects.equals(reporterId, ownerId)) {
            return renterId;
        }
        if (Objects.equals(reporterId, renterId)) {
            return ownerId;
        }
        throw new NotLoanParticipantException("User " + reporterId + " is not a participant of loan " + loanId);
    }

    public void updateDetails(String title, ReportType type, String description, URI evidenceUrl) {
        if (this.status != ReportStatus.PENDING) {
            throw new InvalidReportStateException("Only reports in PENDING status can be changed");
        }
        this.title = title;
        this.type = type;
        this.description = description;
        this.evidenceUrl = evidenceUrl;
    }

    public void assignToReview() {
        transitionTo(ReportStatus.IN_REVIEW);
    }

    public void resolve(ReportStatus resolution, String comment, ReportAction verdict) {
        validateVerdict(resolution, verdict);
        transitionTo(resolution);
        this.moderatorComment = comment;
        this.moderatorVerdict = verdict;
    }

    private void transitionTo(ReportStatus target) {
        if (!this.status.canChangeStatus(target)) {
            throw new InvalidReportStateException(
                    "Cannot transition report from " + this.status + " to " + target);
        }
        this.status = target;
    }

    private void validateVerdict(ReportStatus resolution, ReportAction verdict) {
        if (resolution == ReportStatus.REJECTED && verdict != ReportAction.NONE) {
            throw new InvalidReportResolutionException("Rejected report must have action NONE");
        }
        if (resolution == ReportStatus.RESOLVED && verdict == ReportAction.NONE) {
            throw new InvalidReportResolutionException("Resolved report must have an actionable verdict");
        }
    }
}