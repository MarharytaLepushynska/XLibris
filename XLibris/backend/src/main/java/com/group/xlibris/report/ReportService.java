package com.group.xlibris.report;

import com.group.xlibris.report.dto.ReportFilterCriteria;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.internal.command.CreateLoanReportCommand;
import com.group.xlibris.report.internal.command.CreateReportCommand;
import com.group.xlibris.report.internal.command.ResolveReportCommand;
import com.group.xlibris.report.internal.command.UpdateReportCommand;

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
