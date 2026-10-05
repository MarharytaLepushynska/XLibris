package com.group.xlibris.report.internal;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.user.User;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.report.ReportService;
import com.group.xlibris.report.dto.ReportFilterCriteria;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.ReportStatus;
import com.group.xlibris.report.ReportCreatedEvent;
import com.group.xlibris.report.ReportRejectedEvent;
import com.group.xlibris.report.ReportResolvedEvent;
import com.group.xlibris.report.internal.command.CreateLoanReportCommand;
import com.group.xlibris.report.internal.command.CreateReportCommand;
import com.group.xlibris.report.internal.command.ResolveReportCommand;
import com.group.xlibris.report.internal.command.UpdateReportCommand;
import com.group.xlibris.user.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final LoanService loanService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    public ReportServiceImpl(ReportRepository reportRepository, LoanService loanService, UserService userService, ApplicationEventPublisher eventPublisher) {
        this.reportRepository = reportRepository;
        this.loanService = loanService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReportResponse getReportById(UUID id) {

        log.debug("Finding report by id={}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        return ReportResponse.from(report);
    }

    @Override
    public List<ReportResponse> getAllReports(ReportFilterCriteria criteria) {

        log.debug("Fetching reports: status={}, loanId={}, reporterId={}, targetUserId={}", criteria.status(), criteria.loanId(), criteria.reporterId(), criteria.targetUserId());

        return reportRepository.findReportsByCriteria(
                        criteria.status(), criteria.loanId(),
                        criteria.reporterId(), criteria.targetUserId())
                .stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Override
    public List<ReportResponse> getAllReportsForLoan(UUID loanId) {
        loanService.getLoanById(loanId);
        return getAllReports(new ReportFilterCriteria(null, loanId, null, null));
    }

    @Override
    public ReportResponse createReportForLoan(UUID loanId, CreateLoanReportCommand command) {
        LoanResponse loanDto = loanService.getLoanById(loanId);

        Loan loanProxy = loanService.getLoanReferenceById(loanId);
        User ownerProxy = userService.getUserReferenceById(loanDto.ownerId());
        User renterProxy = userService.getUserReferenceById(loanDto.renterId());
        User reporterProxy = userService.getUserReferenceById(command.reporterId());

        Report report = Report.forLoan(
                loanProxy, ownerProxy, renterProxy, reporterProxy,
                command.title(), command.type(),
                command.description(), command.evidenceUrl());
        Report savedReport = reportRepository.save(report);

        log.info("Report with id={} was created", savedReport.getId());

        eventPublisher.publishEvent(new ReportCreatedEvent(
                savedReport.getId(),
                savedReport.getReporter().getId(),
                savedReport.getTargetUser().getId(),
                savedReport.getLoan().getId(),
                savedReport.getType(),
                savedReport.getTitle(),
                savedReport.getCreatedAt()
        ));

        log.info("Event for creating report was published");

        return ReportResponse.from(savedReport);
    }

    @Override
    public ReportResponse createReportStandalone(CreateReportCommand command) {
        User reporterProxy = userService.getUserReferenceById(command.reporterId());
        User targetUserProxy = userService.getUserReferenceById(command.targetUserId());

        Report report = Report.standalone(
                command.title(), command.type(), command.description(),
                command.evidenceUrl(), reporterProxy, targetUserProxy);
        Report savedReport = reportRepository.save(report);

        log.info("Report with id={} was created", savedReport.getId());

        eventPublisher.publishEvent(new ReportCreatedEvent(
                savedReport.getId(),
                savedReport.getReporter().getId(),
                savedReport.getTargetUser().getId(),
                null,
                savedReport.getType(),
                savedReport.getTitle(),
                savedReport.getCreatedAt()
        ));

        log.info("Event for creating report was published");

        return ReportResponse.from(savedReport);
    }

    @Override
    public ReportResponse updateReport(UUID id, UpdateReportCommand command) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));

        report.updateDetails(
                command.title(),
                command.type(),
                command.description(),
                command.evidenceUrl()
        );

        log.info("Report information with id={} was updated", id);

        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    public ReportResponse assignToReview(UUID id) {
        Report report = reportRepository.findById(id).
                orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        ReportStatus previousStatus = report.getStatus();
        report.assignToReview();
        Report saved = reportRepository.save(report);

        log.info("Status of Report with id={} was changed from {} to {}", id, previousStatus, saved.getStatus());

        return ReportResponse.from(saved);
    }

    @Override
    public ReportResponse resolveReport(UUID id, ResolveReportCommand command) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        ReportStatus previousStatus = report.getStatus();
        report.resolve(command.resolution(), command.moderatorComment(), command.moderatorVerdict());
        Report saved = reportRepository.save(report);

        log.info("Status of Report with id={} was changed from {} to {}", id, previousStatus, saved.getStatus());

        if (saved.getStatus() == ReportStatus.REJECTED) {
            eventPublisher.publishEvent(new ReportRejectedEvent(
                    saved.getId(),
                    saved.getReporter().getId(),
                    saved.getTargetUser().getId(),
                    saved.getModeratorComment(),
                    Instant.now()
            ));

            log.info("Event for rejecting report was published");
        } else if (saved.getStatus() == ReportStatus.RESOLVED) {
            eventPublisher.publishEvent(new ReportResolvedEvent(
                    saved.getId(),
                    saved.getReporter().getId(),
                    saved.getTargetUser().getId(),
                    saved.getModeratorVerdict(),
                    saved.getModeratorComment(),
                    Instant.now()
            ));

            log.info("Event for resolving report was published");
        }

        return ReportResponse.from(saved);
    }

    @Override
    public void deleteReport(UUID id) {
        if (!reportRepository.existsById(id)) {
            throw new NotFoundException("Report (" + id + ") was not found");
        }
        reportRepository.deleteById(id);

        log.info("Report with id={} was deleted", id);
    }
}
