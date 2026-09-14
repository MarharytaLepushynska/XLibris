package com.group.xlibris.report.controller;

import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.report.dto.LoanReportRequest;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.entity.Report;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/loans/{loanId}/reports")
public class LoanReportController {
    private final Map<UUID, Report> reports;
    private final Map<UUID, Loan> loans;

    public LoanReportController() {
        reports = new HashMap<>();
        loans = new HashMap<>();
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllForLoan(@PathVariable UUID loanId) {
        if (!loans.containsKey(loanId)) {
            throw new NotFoundException("Loan with id " + loanId + " not found");
        }

        List<ReportResponse> responseList = reports.values().stream()
                .filter(report -> Objects.equals(report.getLoanId(), loanId))
                .map(ReportResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @PostMapping
    public ResponseEntity<ReportResponse> createReportForLoan(@PathVariable UUID loanId, @Valid @RequestBody LoanReportRequest request) {
        Loan loan = loans.get(loanId);

        if (loan == null) {
            throw new NotFoundException("Loan with id " + loanId + " not found");
        }

        Report report = Report.forLoan(loan, request.reporterId(), request.title(),
                request.type(), request.description(), request.evidenceUrl());
        reports.put(report.getId(), report);
        ReportResponse reportResponse = ReportResponse.from(report);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reports/{id}")
                .buildAndExpand(report.getId())
                .toUri();

        return ResponseEntity.created(location).body(reportResponse);
    }
}
