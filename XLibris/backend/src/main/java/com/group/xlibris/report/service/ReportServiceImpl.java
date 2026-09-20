package com.group.xlibris.report.service;

import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.service.LoanService;
import com.group.xlibris.report.command.*;
import com.group.xlibris.report.dto.ReportFilterCriteria;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final LoanService loanService;

    public ReportServiceImpl(ReportRepository reportRepository, LoanService loanService) {
        this.reportRepository = reportRepository;
        this.loanService = loanService;
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
        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    public ReportResponse createReportStandalone(CreateReportCommand command) {
        Report report = Report.standalone(
                command.title(), command.type(), command.description(),
                command.evidenceUrl(), command.reporterId(), command.targetUserId());
        return ReportResponse.from(reportRepository.save(report));
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
        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    public void deleteReport(UUID id) {
        if (!reportRepository.existsById(id)) {
            throw new NotFoundException("Report (" + id + ") was not found");
        }
        reportRepository.deleteById(id);
    }
}
