package com.group.xlibris.report;

import com.group.xlibris.report.dto.*;
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
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints for managing user reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID", description = "Retrieves detailed information about the report by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Report was not found")
    })
    public ResponseEntity<ReportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping
    @Operation(summary = "Get all reports", description = "Retrieves a filtered list of reports based on criteria like status, loan, reporter or target user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of reports successfully retrieved")
    })
    public ResponseEntity<List<ReportResponse>> getAll(ReportFilterCriteria criteria) {
        return ResponseEntity.ok(reportService.getAllReports(criteria));
    }

    @PostMapping(produces = "application/json")
    @Operation(summary = "Create standalone report", description = "Creates a general user report against another user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Report successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error or self-reporting attempt")
    })
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest reportRequest) {
        ReportResponse response = reportService.createReportStandalone(reportRequest.toCreateCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update report details", description = "Updates details of an existing report while it is still in PENDING status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid report state or validation error"),
            @ApiResponse(responseCode = "404", description = "Report was not found")
    })
    public ResponseEntity<ReportResponse> updateDetails(@PathVariable UUID id, @Valid @RequestBody UpdateReportRequest request) {
        return ResponseEntity.ok(reportService.updateReport(id, request.toCommand()));
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Assign report to review", description = "Transitions report status to IN_REVIEW for moderator assessment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report status changed to IN_REVIEW"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Report was not found")
    })
    public ResponseEntity<ReportResponse> assignToReview(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.assignToReview(id));
    }

    @PatchMapping("/{id}/resolution")
    @Operation(summary = "Resolve or reject report", description = "Adds moderator resolution, verdict and comments to finalize the report")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report successfully resolved"),
            @ApiResponse(responseCode = "400", description = "Invalid resolution rules or state transition"),
            @ApiResponse(responseCode = "404", description = "Report was not found")
    })
    public ResponseEntity<ReportResponse> resolve(@PathVariable UUID id, @Valid @RequestBody ReportResolutionRequest resolutionRequest) {
        return ResponseEntity.ok(reportService.resolveReport(id, resolutionRequest.toCommand()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete report", description = "Removes a report by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Report was not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
