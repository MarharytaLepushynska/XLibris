package com.group.xlibris.report.controller;

import com.group.xlibris.report.dto.LoanReportRequest;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/loans/{loanId}/reports")
public class LoanReportController {
    private final ReportService reportService;

    public LoanReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllForLoan(@PathVariable UUID loanId) {
        return ResponseEntity.ok(reportService.getAllReportsForLoan(loanId));
    }

    @PostMapping
    public ResponseEntity<ReportResponse> createReportForLoan(@PathVariable UUID loanId, @Valid @RequestBody LoanReportRequest request) {
        ReportResponse response = reportService.createReportForLoan(loanId, request.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reports/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
