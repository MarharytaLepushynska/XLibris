package com.group.xlibris.report.controller;

import com.group.xlibris.report.dto.ReportRequest;
import com.group.xlibris.report.dto.ReportResolutionRequest;
import com.group.xlibris.report.dto.UpdateReportRequest;
import com.group.xlibris.report.entity.Report;
import com.group.xlibris.report.enums.ReportAction;
import com.group.xlibris.report.enums.ReportStatus;
import com.group.xlibris.report.enums.ReportType;
import com.group.xlibris.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportRepository reportRepository;

    private final UUID pendingReportId = UUID.fromString("550e8400-e29b-41d4-a716-446655441000");
    private final UUID inReviewReportId = UUID.fromString("550e8400-e29b-41d4-a716-446655441001");

    private final UUID reporterId = UUID.fromString("550e8400-e29b-41d4-a716-446655442000");
    private final UUID reporterId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655442001");

    private final UUID targetUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655443000");
    private final UUID targetUserId2 = UUID.fromString("550e8400-e29b-41d4-a716-446655443001");

    @BeforeEach
    void resetMap() {
        reportRepository.deleteAll();

        Report pendingReport = new Report(
                pendingReportId,
                "Book is damaged",
                ReportType.DAMAGED_BOOK,
                "Pages are died from tea",
                URI.create("https://example.com/proof.jpg"),
                Instant.now(),
                null,
                reporterId,
                targetUserId,
                ReportStatus.PENDING,
                null,
                null
        );

        Report inReviewReport = new Report(
                inReviewReportId,
                "Inappropriate behaviour from owner",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Owner was offensive",
                URI.create("https://example.com/proof.jpg"),
                Instant.now(),
                null,
                reporterId2,
                targetUserId2,
                ReportStatus.IN_REVIEW,
                null,
                null
        );

        reportRepository.save(pendingReport);
        reportRepository.save(inReviewReport);
    }

    @Test
    void shouldCreateReport() throws Exception {
        ReportRequest request = new ReportRequest(
                "Inappropriate behaviour from owner",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Owner was offensive",
                URI.create("https://example.com/proof.jpg"),
                reporterId,
                targetUserId
        );

        mvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Inappropriate behaviour from owner"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldGetReportById() throws Exception {
        mvc.perform(get("/api/reports/{id}", pendingReportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pendingReportId.toString()))
                .andExpect(jsonPath("$.title").value("Book is damaged"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldGetAllReports() throws Exception {
        mvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFilterReportsByStatus() throws Exception {
        mvc.perform(get("/api/reports")
                        .param("status", "IN_REVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(inReviewReportId.toString()));
    }

    @Test
    void shouldFilterReportsByReporterId() throws Exception {
        mvc.perform(get("/api/reports")
                        .param("reporterId", reporterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reporterId").value(reporterId.toString()));
    }

    @Test
    void shouldFilterReportsByTargetUserId() throws Exception {
        mvc.perform(get("/api/reports")
                        .param("targetUserId", targetUserId2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].targetUserId").value(targetUserId2.toString()));
    }

    @Test
    void shouldUpdateReportDetails() throws Exception {
        UpdateReportRequest updateRequest = new UpdateReportRequest(
                "New title",
                ReportType.DAMAGED_BOOK,
                "New description",
                URI.create("https://example.com/proof.jpg")
        );

        mvc.perform(patch("/api/reports/{id}", pendingReportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pendingReportId.toString()))
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("New description"));
    }

    @Test
    void shouldAssignReportToReview() throws Exception {
        mvc.perform(patch("/api/reports/{id}/review", pendingReportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pendingReportId.toString()))
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));
    }

    @Test
    void shouldResolveReport() throws Exception {
        ReportResolutionRequest resolutionRequest = new ReportResolutionRequest(
                ReportStatus.RESOLVED,
                "User was warned",
                ReportAction.WARNING
        );

        mvc.perform(patch("/api/reports/{id}/resolution", inReviewReportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolutionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inReviewReportId.toString()))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.moderatorVerdict").value("WARNING"));
    }

    @Test
    void shouldDeleteReport() throws Exception {
        mvc.perform(delete("/api/reports/{id}", pendingReportId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequestWhenSelfReporting() throws Exception {
        ReportRequest selfReportRequest = new ReportRequest(
                "Self reporting issue",
                ReportType.DAMAGED_BOOK,
                "Trying to report myself",
                URI.create("https://example.com/proof.jpg"),
                reporterId,
                reporterId
        );

        mvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfReportRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Self report violation"));
    }

    @Test
    void shouldReturnValidationProblemDetailWhenFieldIsMissingOnCreate() throws Exception {
        ReportRequest invalidRequest = new ReportRequest(
                "",
                ReportType.INAPPROPRIATE_BEHAVIOR,
                "Description",
                URI.create("https://example.com/proof.jpg"),
                null,
                targetUserId
        );

        mvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.reporterId").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentReport() throws Exception {
        UUID nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655449999");
        mvc.perform(get("/api/reports/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentReport() throws Exception {
        UUID nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655449999");
        mvc.perform(delete("/api/reports/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void shouldReturnUnreadableProblemDetail() throws Exception {
        mvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ Invalid body }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }
}