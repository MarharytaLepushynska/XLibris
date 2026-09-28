package com.group.xlibris.report.internal;

import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.report.ReportAction;
import com.group.xlibris.report.ReportStatus;
import com.group.xlibris.report.ReportType;
import com.group.xlibris.report.InvalidReportResolutionException;
import com.group.xlibris.report.InvalidReportStateException;
import com.group.xlibris.report.NotLoanParticipantException;
import com.group.xlibris.report.SelfReportException;
import com.group.xlibris.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Entity
@Table(name = "reports")
public class Report {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private URI evidenceUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    private String moderatorComment;

    @Enumerated(EnumType.STRING)
    private ReportAction moderatorVerdict;

    protected Report() {}

    public static Report forLoan(Loan loan, User owner,
                                 User renter, User reporter,
                                 String title, ReportType type,
                                 String description, URI evidenceUrl) {
        User targetUser = resolveOpponent(loan, owner, renter, reporter);
        if (Objects.equals(reporter.getId(), targetUser.getId())) {
            throw new SelfReportException("User cannot report themselves");
        }

        return new Report(
                UUID.randomUUID(), title, type,
                description, evidenceUrl, Instant.now(),
                loan, reporter, targetUser,
                ReportStatus.PENDING, null, null);
    }

    public static Report standalone(String title, ReportType type,
                                    String description, URI evidenceUrl,
                                    User reporter, User targetUser) {
        if (Objects.equals(reporter.getId(), targetUser.getId())) {
            throw new SelfReportException("User cannot report themselves");
        }

        return new Report(
                UUID.randomUUID(), title, type,
                description, evidenceUrl, Instant.now(),
                null, reporter, targetUser,
                ReportStatus.PENDING, null, null);
    }

    private static User resolveOpponent(Loan loan, User owner, User renter, User reporter) {
        if (Objects.equals(reporter.getId(), owner.getId())) {
            return renter;
        }
        if (Objects.equals(reporter.getId(), renter.getId())) {
            return owner;
        }
        throw new NotLoanParticipantException("User " + reporter.getId() + " is not a participant of loan " + loan.getId());
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