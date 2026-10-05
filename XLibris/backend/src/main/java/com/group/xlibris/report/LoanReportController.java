package com.group.xlibris.report;

import com.group.xlibris.report.dto.LoanReportRequest;
import com.group.xlibris.report.dto.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/loans/{loanId}/reports")
@Tag(name = "Loan reports", description = "Endpoints for managing reports directly connected to book loans")
public class LoanReportController {
    private final ReportService reportService;

    public LoanReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "Get all reports for a loan", description = "Retrieves all reports and issues associated with a book loan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of loan reports successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Loan was not found")
    })
    public ResponseEntity<List<ReportResponse>> getAllForLoan(@PathVariable UUID loanId) {
        return ResponseEntity.ok(reportService.getAllReportsForLoan(loanId));
    }

    @PostMapping
    @Operation(summary = "Create report for a loan", description = "Registers a new incident report for the loan between participants")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan report successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error, self-reporting or user is not a loan participant"),
            @ApiResponse(responseCode = "404", description = "Loan was not found")
    })
    public ResponseEntity<ReportResponse> createReportForLoan(@PathVariable UUID loanId, @Valid @RequestBody LoanReportRequest request) {
        ReportResponse response = reportService.createReportForLoan(loanId, request.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reports/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
