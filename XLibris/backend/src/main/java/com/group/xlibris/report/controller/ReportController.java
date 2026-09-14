package com.group.xlibris.report.controller;

import com.group.xlibris.common.exception.IdMismatch;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import com.group.xlibris.report.dto.ReportRequest;
import com.group.xlibris.report.dto.ReportResolutionRequest;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.enums.ReportStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final Map<UUID, Report> reports;

    public ReportController() {
        reports = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getById(@PathVariable UUID id) {
        Report report = findReportById(id);
        ReportResponse reportResponse = ReportResponse.from(report);
        return ResponseEntity.ok(reportResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAll(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) UUID loanId,
            @RequestParam(required = false) UUID reporterId,
            @RequestParam(required = false) UUID targetUserId
    ) {
        List<ReportResponse> responseList = reports.values().stream()
                .filter(report -> status == null || report.getStatus() == status)
                .filter(report -> loanId == null || Objects.equals(loanId, report.getLoanId()))
                .filter(report -> reporterId == null || report.getReporterId().equals(reporterId))
                .filter(report -> targetUserId == null || report.getTargetUserId().equals(targetUserId))
                .map(ReportResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<ReportResponse> create(@Validated(OnCreate.class) @RequestBody ReportRequest reportRequest) {
        Report report = Report.standalone(
                reportRequest.title(),
                reportRequest.type(),
                reportRequest.description(),
                reportRequest.evidenceUrl(),
                reportRequest.reporterId(),
                reportRequest.targetUserId());
        reports.put(report.getId(), report);

        ReportResponse reportResponse = ReportResponse.from(report);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reportResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(reportResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportResponse> update(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody ReportRequest reportRequest) {
        if (!id.equals(reportRequest.id())) {
            throw new IdMismatch("Id mismatch");
        }

        Report report = findReportById(id);
        report.updateDetails(
                reportRequest.title(),
                reportRequest.type(),
                reportRequest.description(),
                reportRequest.evidenceUrl()
        );

        ReportResponse reportResponse = ReportResponse.from(report);
        return ResponseEntity.ok(reportResponse);
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ReportResponse> assignToReview(@PathVariable UUID id) {
        Report report = findReportById(id);
        report.assignToReview();
        return ResponseEntity.ok(ReportResponse.from(report));
    }

    @PatchMapping("/{id}/resolution")
    public ResponseEntity<ReportResponse> resolve(@PathVariable UUID id, @Valid @RequestBody ReportResolutionRequest resolutionRequest) {
        Report report = findReportById(id);
        report.resolve(
                resolutionRequest.resolution(),
                resolutionRequest.moderatorComment(),
                resolutionRequest.moderatorVerdict()
        );
        return ResponseEntity.ok(ReportResponse.from(report));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (reports.remove(id) == null) {
            throw new NotFoundException("Report with id " + id + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    private Report findReportById(UUID id) {
        Report report = reports.get(id);
        if (report == null) {
            throw new NotFoundException("Report with id " + id + " not found");
        }
        return report;
    }
}
