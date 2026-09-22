package com.group.xlibris.report.repository;

import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.enums.ReportStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository {
    Report save(Report report);
    Optional<Report> findById(UUID id);
    List<Report> findAll(ReportStatus status, UUID loanId, UUID reporterId, UUID targetUserId);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    void deleteAll();
}
