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

        System.out.println("Report with id " + savedReport.getId() + " was created");

        eventPublisher.publishEvent(new ReportCreatedEvent(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getTargetUserId(),
                savedReport.getLoanId(),
                savedReport.getType(),
                savedReport.getTitle(),
                savedReport.getCreatedAt()
        ));

        System.out.println("Event for creating report was published");

        return ReportResponse.from(savedReport);
    }

    @Override
    public ReportResponse createReportStandalone(CreateReportCommand command) {
        Report report = Report.standalone(
                command.title(), command.type(), command.description(),
                command.evidenceUrl(), command.reporterId(), command.targetUserId());
        Report savedReport = reportRepository.save(report);

        System.out.println("Report with id " + savedReport.getId() + " was created");

        eventPublisher.publishEvent(new ReportCreatedEvent(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getTargetUserId(),
                null,
                savedReport.getType(),
                savedReport.getTitle(),
                savedReport.getCreatedAt()
        ));

        System.out.println("Event for creating report was published");

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

        System.out.println("Report information with id " + id + " was updated");

        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    public ReportResponse assignToReview(UUID id) {
        Report report = reportRepository.findById(id).
                orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        ReportStatus previousStatus = report.getStatus();
        report.assignToReview();
        Report saved = reportRepository.save(report);

        System.out.println("Status of Report with id " + id + " was changed from " + previousStatus + " to " + saved.getStatus());

        return ReportResponse.from(saved);
    }

    @Override
    public ReportResponse resolveReport(UUID id, ResolveReportCommand command) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report (" + id + ") was not found"));
        ReportStatus previousStatus = report.getStatus();
        report.resolve(command.resolution(), command.moderatorComment(), command.moderatorVerdict());
        Report saved = reportRepository.save(report);

        System.out.println("Status of Report with id " + id + " was changed from " + previousStatus + " to " + saved.getStatus());

        if (saved.getStatus() == ReportStatus.REJECTED) {
            eventPublisher.publishEvent(new ReportRejectedEvent(
                    saved.getId(),
                    saved.getReporterId(),
                    saved.getTargetUserId(),
                    saved.getModeratorComment(),
                    Instant.now()
            ));

            System.out.println("Event for rejecting report was published");
        } else if (saved.getStatus() == ReportStatus.RESOLVED) {
            eventPublisher.publishEvent(new ReportResolvedEvent(
                    saved.getId(),
                    saved.getReporterId(),
                    saved.getTargetUserId(),
                    saved.getModeratorVerdict(),
                    saved.getModeratorComment(),
                    Instant.now()
            ));

            System.out.println("Event for resolving report was published");
        }

        return ReportResponse.from(saved);
    }

    @Override
    public void deleteReport(UUID id) {
        if (!reportRepository.existsById(id)) {
            throw new NotFoundException("Report (" + id + ") was not found");
        }
        reportRepository.deleteById(id);

        System.out.println("Report with id " + id + " was deleted");
    }
}
