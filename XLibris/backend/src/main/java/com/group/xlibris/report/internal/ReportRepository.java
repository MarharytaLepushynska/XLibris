package com.group.xlibris.report.internal;

import com.group.xlibris.report.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    @Query("SELECT r FROM Report r " +
            "JOIN FETCH r.reporter " +
            "JOIN FETCH r.targetUser " +
            "LEFT JOIN FETCH r.loan " +
            "WHERE (:status IS NULL OR r.status = :status) AND " +
            "(:loanId IS NULL OR r.loan.id = :loanId) AND " +
            "(:reporterId IS NULL OR r.reporter.id = :reporterId) AND " +
            "(:targetUserId IS NULL OR r.targetUser.id = :targetUserId)")
    List<Report> findReportsByCriteria(@Param("status") ReportStatus status,
                                       @Param("loanId") UUID loanId,
                                       @Param("reporterId") UUID reporterId,
                                       @Param("targetUserId") UUID targetUserId);
}
