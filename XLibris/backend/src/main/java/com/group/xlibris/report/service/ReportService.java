package com.group.xlibris.report.service;

import com.group.xlibris.report.command.*;
import com.group.xlibris.report.dto.ReportFilterCriteria;
import com.group.xlibris.report.dto.ReportResponse;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    ReportResponse getReportById(UUID id);
    List<ReportResponse> getAllReports(ReportFilterCriteria criteria);
    List<ReportResponse> getAllReportsForLoan(UUID loanId);
    ReportResponse createReportForLoan(UUID loanId, CreateLoanReportCommand command);
    ReportResponse createReportStandalone(CreateReportCommand command);
    ReportResponse updateReport(UUID id, UpdateReportCommand command);
    ReportResponse assignToReview(UUID id);
    ReportResponse resolveReport(UUID id, ResolveReportCommand command);
    void deleteReport(UUID id);
}
