package com.group.xlibris.report.controller;

import com.group.xlibris.common.exception.IdMismatch;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import com.group.xlibris.report.dto.ReportRequest;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.enums.ReportStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final Map<UUID, Report> reports;

    public ReportController() {
        reports = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getById(@PathVariable UUID id) {
        Report report = reports.get(id);
        if (report == null) {
            throw new NotFoundException("Report with id " + id + " not found");
        }
        ReportResponse reportResponse = toResponse(report);
        return ResponseEntity.ok(reportResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAll() {
        List<ReportResponse> responseList = reports.values().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<ReportResponse> create(@Validated(OnCreate.class) @RequestBody ReportRequest reportRequest) {
        Report report = createReport(reportRequest);
        reports.put(report.id(), report);

        ReportResponse reportResponse = toResponse(report);

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

        if (!reports.containsKey(reportRequest.id())) {
            throw new NotFoundException("Report with id " + id + " not found");
        }

        Report oldReport = reports.get(id);
        ReportStatus status = (reportRequest.status() != null)
                ? reportRequest.status()
                : oldReport.status();

        Report updatedReport = new Report(
                id,
                reportRequest.title(),
                reportRequest.type(),
                reportRequest.description(),
                reportRequest.evidenceUrl(),
                oldReport.createdAt(),
                oldReport.reporterId(),
                oldReport.targetUserId(),
                status
        );
        reports.put(id, updatedReport);

        ReportResponse reportResponse = toResponse(updatedReport);
        return ResponseEntity.ok(reportResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!reports.containsKey(id)) {
            throw new NotFoundException("Report with id " + id + " not found");
        }
        reports.remove(id);
        return ResponseEntity.noContent().build();
    }

    private ReportResponse toResponse(Report report) {
        return new ReportResponse(
                report.id(),
                report.title(),
                report.type(),
                report.description(),
                report.evidenceUrl(),
                report.createdAt(),
                report.reporterId(),
                report.targetUserId(),
                report.status());
    }

    private Report createReport(ReportRequest reportRequest) {
        UUID reportId = UUID.randomUUID();
        return new Report(reportId,
                reportRequest.title(),
                reportRequest.type(),
                reportRequest.description(),
                reportRequest.evidenceUrl(),
                Instant.now(),
                reportRequest.reporterId(),
                reportRequest.targetUserId(),
                ReportStatus.PENDING);
    }
}
