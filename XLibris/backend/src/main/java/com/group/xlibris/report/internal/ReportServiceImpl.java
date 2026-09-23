package com.group.xlibris.report.internal;

import com.group.xlibris.common.NotFoundException;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final LoanService loanService;
    private final ApplicationEventPublisher eventPublisher;

    public ReportServiceImpl(ReportRepository reportRepository, LoanService loanService, ApplicationEventPublisher eventPublisher) {
        this.reportRepository = reportRepository;
        this.loanService = loanService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReportResponse getReportById(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        return ReportResponse.from(report);
    }

    @Override
    public List<ReportResponse> getAllReports(ReportFilterCriteria criteria) {
        return reportRepository.findAll(criteria.status(), criteria.loanId(), criteria.reporterId(), criteria.targetUserId())
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
        LoanResponse loan = loanService.getLoanById(loanId);
        Report report = Report.forLoan(
                loan.id(), loan.ownerId(), loan.renterId(),
                command.reporterId(), command.title(), command.type(),
                command.description(), command.evidenceUrl());
        Report savedReport = reportRepository.save(report);

        eventPublisher.publishEvent(new ReportCreatedEvent(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getTargetUserId(),
                savedReport.getLoanId(),
                savedReport.getType(),
                savedReport.getTitle(),
                savedReport.getCreatedAt()
        ));
        return ReportResponse.from(savedReport);
    }

    @Override
    public ReportResponse createReportStandalone(CreateReportCommand command) {
        Report report = Report.standalone(
                command.title(), command.type(), command.description(),
                command.evidenceUrl(), command.reporterId(), command.targetUserId());
        Report savedReport = reportRepository.save(report);

        eventPublisher.publishEvent(new ReportCreatedEvent(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getTargetUserId(),
                null,
                savedReport.getType(),
                savedReport.getTitle(),
                savedReport.getCreatedAt()
        ));

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
        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    public ReportResponse assignToReview(UUID id) {
        Report report = reportRepository.findById(id).
                orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        report.assignToReview();
        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    public ReportResponse resolveReport(UUID id, ResolveReportCommand command) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        report.resolve(command.resolution(), command.moderatorComment(), command.moderatorVerdict());
        Report savedReport = reportRepository.save(report);

        if (savedReport.getStatus() == ReportStatus.REJECTED) {
            eventPublisher.publishEvent(new ReportRejectedEvent(
                    savedReport.getId(),
                    savedReport.getReporterId(),
                    savedReport.getTargetUserId(),
                    savedReport.getModeratorComment(),
                    Instant.now()
            ));
        } else if (savedReport.getStatus() == ReportStatus.RESOLVED) {
            eventPublisher.publishEvent(new ReportResolvedEvent(
                    savedReport.getId(),
                    savedReport.getReporterId(),
                    savedReport.getTargetUserId(),
                    savedReport.getModeratorVerdict(),
                    savedReport.getModeratorComment(),
                    Instant.now()
            ));
        }

        return ReportResponse.from(savedReport);
    }

    @Override
    public void deleteReport(UUID id) {
        if (!reportRepository.existsById(id)) {
            throw new NotFoundException("Report (" + id + ") was not found");
        }
        reportRepository.deleteById(id);
    }
}
