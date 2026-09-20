package com.group.xlibris.report.repository;

import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.enums.ReportStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ReportRepositoryImpl implements ReportRepository {
    private final Map<UUID, Report> reports = new ConcurrentHashMap<>();

    @Override
    public Report save(Report report) {
        reports.put(report.getId(), report);
        return report;
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return Optional.ofNullable(reports.get(id));
    }

    @Override
    public List<Report> findAll(ReportStatus status, UUID loanId, UUID reporterId, UUID targetUserId) {
        return reports.values().stream()
                .filter(report -> status == null || report.getStatus().equals(status))
                .filter(report -> loanId == null || Objects.equals(loanId, report.getLoanId()))
                .filter(report -> reporterId == null || report.getReporterId().equals(reporterId))
                .filter(report -> targetUserId == null || report.getTargetUserId().equals(targetUserId))
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return reports.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        reports.remove(id);
    }
}
