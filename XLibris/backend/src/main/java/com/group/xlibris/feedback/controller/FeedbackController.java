package com.group.xlibris.feedback.controller;

import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import com.group.xlibris.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getById(
            @PathVariable("id") UUID id
    ) {
        FeedbackResponse response = feedbackService.getById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAll(
            @RequestParam(name = "loanId", required = false) UUID loanId,
            @RequestParam(name = "reviewerId", required = false) UUID reviewerId,
            @RequestParam(name = "reviewedUserId", required = false) UUID reviewedUserId
    ) {
        List<FeedbackResponse> feedbacks =
                feedbackService.getAll(
                        loanId,
                        reviewerId,
                        reviewedUserId
                );

        return ResponseEntity.ok(feedbacks);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<FeedbackResponse> create(
            @Valid @RequestBody FeedbackRequest request
    ) {
        FeedbackResponse response =
                feedbackService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }
}