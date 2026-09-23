package com.group.xlibris.report.service;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.enums.LoanStatus;
import com.group.xlibris.loan.service.LoanService;
import com.group.xlibris.report.command.*;
import com.group.xlibris.report.dto.ReportFilterCriteria;
import com.group.xlibris.report.dto.ReportResponse;
import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.enums.ReportAction;
import com.group.xlibris.report.enums.ReportStatus;
import com.group.xlibris.report.enums.ReportType;
import com.group.xlibris.report.exception.InvalidReportResolutionException;
import com.group.xlibris.report.exception.InvalidReportStateException;
import com.group.xlibris.report.exception.NotLoanParticipantException;
import com.group.xlibris.report.exception.SelfReportException;
import com.group.xlibris.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private LoanService loanService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ReportServiceImpl reportService;

    private UUID reportId;
    private UUID loanId;
    private UUID ownerId;
    private UUID renterId;
    private UUID targetUserId;
    private Report pendingReport;
    private LoanResponse mockLoanResponse;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(reportRepository, loanService, eventPublisher);

        reportId = UUID.randomUUID();
        loanId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();

        pendingReport = new Report(
                reportId,
                "Damaged pages",
                ReportType.DAMAGED_BOOK,
                "Several pages are torn",
                URI.create("https://example.com/proof.jpg"),
                Instant.now(),
                null,
                ownerId,
                targetUserId,
                ReportStatus.PENDING,
                null,
                null
        );

        mockLoanResponse = new LoanResponse(
                loanId,
                UUID.randomUUID(),
                ownerId,
                renterId,
                Instant.now().minusSeconds(86400 * 5),
                Instant.now().plusSeconds(86400 * 5),
                null,
                LoanStatus.ACTIVE
        );
    }

    @Test
    void shouldGetByIdSuccessfully() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(pendingReport));

        ReportResponse response = reportService.getReportById(reportId);

        assertNotNull(response);
        assertEquals(reportId, response.id());
        assertEquals("Damaged pages", response.title());
        assertEquals(ReportStatus.PENDING, response.status());

        verify(reportRepository).findById(reportId);
    }

    @Test
    void shouldThrowNotFoundWhenReportMissing() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reportService.getReportById(reportId));
        verify(reportRepository).findById(reportId);
    }

    @Test
    void shouldGetAllReportsSuccessfully() {
        ReportFilterCriteria criteria = new ReportFilterCriteria(ReportStatus.PENDING, null, ownerId, null);
        when(reportRepository.findAll(ReportStatus.PENDING, null, ownerId, null))
                .thenReturn(List.of(pendingReport));

        List<ReportResponse> responses = reportService.getAllReports(criteria);

        assertEquals(1, responses.size());
        assertEquals(reportId, responses.getFirst().id());
        verify(reportRepository).findAll(ReportStatus.PENDING, null, ownerId, null);
    }

    @Test
    void shouldGetAllReportsForLoanSuccessfully() {
        when(loanService.getLoanById(loanId)).thenReturn(mockLoanResponse);
        when(reportRepository.findAll(null, loanId, null, null)).thenReturn(List.of(pendingReport));

        List<ReportResponse> responses = reportService.getAllReportsForLoan(loanId);

        assertEquals(1, responses.size());
        verify(loanService).getLoanById(loanId);
        verify(reportRepository).findAll(null, loanId, null, null);
    }

    @Test
    void shouldCreateReportForLoanSuccessfully() {
        CreateLoanReportCommand command = new CreateLoanReportCommand(
                "Book not returned in time",
                ReportType.OVERDUE_RETURN,
                "Renter refuses to return",
                URI.create("https:/example.com/proof.jpg"),
                ownerId
        );

        when(loanService.getLoanById(loanId)).thenReturn(mockLoanResponse);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportService.createReportForLoan(loanId, command);

        assertNotNull(response);
        assertEquals(loanId, response.loanId());
        assertEquals(ownerId, response.reporterId());
        assertEquals(renterId, response.targetUserId());
        assertEquals(ReportStatus.PENDING, response.status());

        verify(loanService).getLoanById(loanId);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void shouldThrowNotLoanParticipantExceptionWhenReporterIsNotParticipant() {
        UUID outsiderId = UUID.randomUUID();
        CreateLoanReportCommand command = new CreateLoanReportCommand(
                "Some issue",
                ReportType.DAMAGED_BOOK,
                "Description",
                URI.create("https:/example.com/proof.jpg"),
                outsiderId
        );

        when(loanService.getLoanById(loanId)).thenReturn(mockLoanResponse);

        assertThrows(NotLoanParticipantException.class, () -> reportService.createReportForLoan(loanId, command));
        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldCreateReportStandaloneSuccessfully() {
        CreateReportCommand command = new CreateReportCommand(
                "Spam account",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "User sends spam messages",
                URI.create("https:/example.com/proof.jpg"),
                ownerId,
                targetUserId
        );

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportService.createReportStandalone(command);

        assertNotNull(response);
        assertEquals("Spam account", response.title());
        assertEquals(ownerId, response.reporterId());
        assertEquals(targetUserId, response.targetUserId());
        assertNull(response.loanId());

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void shouldThrowSelfReportExceptionWhenReportingSelf() {
        CreateReportCommand command = new CreateReportCommand(
                "Self report",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Description",
                URI.create("https:/example.com/proof.jpg"),
                ownerId,
                ownerId
        );

        assertThrows(SelfReportException.class, () -> reportService.createReportStandalone(command));
        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldUpdateReportSuccessfully() {
        UpdateReportCommand command = new UpdateReportCommand(
                "Updated title",
                ReportType.DAMAGED_BOOK,
                "Updated description",
                URI.create("https:/example.com/new-proof.jpg")
        );

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(pendingReport));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportService.updateReport(reportId, command);

        assertNotNull(response);
        assertEquals("Updated title", response.title());
        assertEquals("Updated description", response.description());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(pendingReport);
    }

    @Test
    void shouldThrowInvalidReportStateExceptionWhenUpdatingNonPendingReport() {
        pendingReport.assignToReview();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(pendingReport));

        UpdateReportCommand command = new UpdateReportCommand(
                "Updated title",
                ReportType.DAMAGED_BOOK,
                "Updated description",
                URI.create("https:/example.com/new-proof.jpg")
        );

        assertThrows(InvalidReportStateException.class, () -> reportService.updateReport(reportId, command));
        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldAssignToReviewSuccessfully() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(pendingReport));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportService.assignToReview(reportId);

        assertNotNull(response);
        assertEquals(ReportStatus.IN_REVIEW, response.status());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(pendingReport);
    }

    @Test
    void shouldResolveReportSuccessfully() {
        pendingReport.assignToReview();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(pendingReport));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResolveReportCommand command = new ResolveReportCommand(
                ReportStatus.RESOLVED,
                "User has been warned",
                ReportAction.WARNING
        );

        ReportResponse response = reportService.resolveReport(reportId, command);

        assertNotNull(response);
        assertEquals(ReportStatus.RESOLVED, response.status());
        assertEquals("User has been warned", response.moderatorComment());
        assertEquals(ReportAction.WARNING, response.moderatorVerdict());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(pendingReport);
    }

    @Test
    void shouldThrowInvalidReportResolutionExceptionWhenVerdictInvalid() {
        pendingReport.assignToReview();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(pendingReport));

        ResolveReportCommand invalidCommand = new ResolveReportCommand(
                ReportStatus.REJECTED,
                "No grounds for report",
                ReportAction.TEMPORARY_BAN
        );

        assertThrows(InvalidReportResolutionException.class, () -> reportService.resolveReport(reportId, invalidCommand));
        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldDeleteReportSuccessfully() {
        when(reportRepository.existsById(reportId)).thenReturn(true);

        reportService.deleteReport(reportId);

        verify(reportRepository).existsById(reportId);
        verify(reportRepository).deleteById(reportId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentReport() {
        when(reportRepository.existsById(reportId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> reportService.deleteReport(reportId));
        verify(reportRepository).existsById(reportId);
        verify(reportRepository, never()).deleteById(any());
    }
}