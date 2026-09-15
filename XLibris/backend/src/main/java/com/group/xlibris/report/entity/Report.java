package com.group.xlibris.report.entity;

import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.report.enums.ReportAction;
import com.group.xlibris.report.enums.ReportStatus;
import com.group.xlibris.report.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
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

    public Report() {
    }

    public static Report forLoan(Loan loan,
                                 UUID reporterId,
                                 String title,
                                 ReportType type,
                                 String description,
                                 URI evidenceUrl) {
        UUID targetUserId = resolveOpponent(loan, reporterId);

        return new Report(
                UUID.randomUUID(),
                title,
                type,
                description,
                evidenceUrl,
                Instant.now(),
                loan.getId(),
                reporterId,
                targetUserId,
                ReportStatus.PENDING,
                null,
                null);
    }

    public static Report standalone(String title,
                                    ReportType type,
                                    String description,
                                    URI evidenceUrl,
                                    UUID reporterId,
                                    UUID targetUserId) {
        if (Objects.equals(reporterId, targetUserId)) {
            throw new IllegalArgumentException("User cannot report themselves");
        }

        return new Report(
                UUID.randomUUID(),
                title,
                type,
                description,
                evidenceUrl,
                Instant.now(),
                null,
                reporterId,
                targetUserId,
                ReportStatus.PENDING,
                null,
                null);
    }

    private static UUID resolveOpponent(Loan loan, UUID reporterId) {
        if (Objects.equals(reporterId, loan.getOwnerId())) {
            return loan.getRenterId();
        }
        if (Objects.equals(reporterId, loan.getRenterId())) {
            return loan.getOwnerId();
        }
        throw new IllegalArgumentException(
                "User " + reporterId + " is not a participant of loan " + loan.getId());
    }

    public void updateDetails(String title, ReportType type, String description, URI evidenceUrl) {
        if (this.status != ReportStatus.PENDING) {
            throw new IllegalStateException("Only reports in PENDING status can be changed");
        }
        this.title = title;
        this.type = type;
        this.description = description;
        this.evidenceUrl = evidenceUrl;
    }

    public void assignToReview() {
        if (this.status != ReportStatus.PENDING) {
            throw new IllegalStateException("Only reports in PENDING status can be taken into review");
        }
        this.status = ReportStatus.IN_REVIEW;
    }

    public void resolve(ReportStatus resolution, String comment, ReportAction verdict) {
        if (this.status != ReportStatus.IN_REVIEW) {
            throw new IllegalStateException("Only reports in IN REVIEW status can be taken into resolution");
        }
        if (resolution != ReportStatus.REJECTED && resolution != ReportStatus.RESOLVED) {
            throw new IllegalArgumentException("Resolution must be either REJECTED or RESOLVED");
        }
        if (resolution == ReportStatus.REJECTED && verdict != ReportAction.NONE) {
            throw new IllegalArgumentException("Rejected report must have action NONE");
        }
        if (resolution == ReportStatus.RESOLVED && verdict == ReportAction.NONE) {
            throw new IllegalArgumentException("Resolved report must have an actionable verdict");
        }

        this.status = resolution;
        this.moderatorComment = comment;
        this.moderatorVerdict = verdict;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof Report report)) return false;
        return Objects.equals(id, report.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}