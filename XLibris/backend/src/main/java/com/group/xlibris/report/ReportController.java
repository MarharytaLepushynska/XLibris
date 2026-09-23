package com.group.xlibris.report;

import com.group.xlibris.report.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAll(ReportFilterCriteria criteria) {
        return ResponseEntity.ok(reportService.getAllReports(criteria));
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest reportRequest) {
        ReportResponse response = reportService.createReportStandalone(reportRequest.toCreateCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReportResponse> updateDetails(@PathVariable UUID id, @Valid @RequestBody UpdateReportRequest request) {
        return ResponseEntity.ok(reportService.updateReport(id, request.toCommand()));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ReportResponse> assignToReview(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.assignToReview(id));
    }

    @PatchMapping("/{id}/resolution")
    public ResponseEntity<ReportResponse> resolve(@PathVariable UUID id, @Valid @RequestBody ReportResolutionRequest resolutionRequest) {
        return ResponseEntity.ok(reportService.resolveReport(id, resolutionRequest.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
