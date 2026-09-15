package com.group.xlibris.feedback.controller;

import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import com.group.xlibris.feedback.entity.Feedback;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final Map<UUID, Feedback> feedbacks;

    public FeedbackController() {
        feedbacks = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getById(@PathVariable UUID id) {

        Feedback feedback = feedbacks.get(id);

        if (feedback == null) {
            throw new NotFoundException(
                    "Feedback with id " + id + " not found"
            );
        }

        return ResponseEntity.ok(
                FeedbackResponse.from(feedback)
        );
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAll(
            @RequestParam(required = false) UUID loanId,
            @RequestParam(required = false) UUID reviewerId,
            @RequestParam(required = false) UUID reviewedUserId
    ) {

        List<FeedbackResponse> responseList = feedbacks.values()
                .stream()
                .filter(feedback ->
                        loanId == null
                        || feedback.loanId().equals(loanId))
                .filter(feedback ->
                        reviewerId == null
                        || feedback.reviewerId().equals(reviewerId))
                .filter(feedback ->
                        reviewedUserId == null
                        || feedback.reviewedUserId().equals(reviewedUserId))
                .map(FeedbackResponse::from)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<FeedbackResponse> create(
            @Valid @RequestBody FeedbackRequest request
    ) {

        if (request.reviewerId().equals(request.reviewedUserId())) {
            throw new IllegalArgumentException(
                    "User cannot leave feedback for themselves"
            );
        }

        boolean feedbackAlreadyExists = feedbacks.values()
                .stream()
                .anyMatch(feedback ->
                        feedback.loanId().equals(request.loanId())
                        && feedback.reviewerId().equals(request.reviewerId())
                );

        if (feedbackAlreadyExists) {
            throw new IllegalStateException(
                    "Feedback for this loan has already been submitted"
            );
        }

        Feedback feedback = new Feedback(
                UUID.randomUUID(),
                request.loanId(),
                request.reviewerId(),
                request.reviewedUserId(),
                request.rating(),
                request.comment(),
                Instant.now()
        );

        feedbacks.put(feedback.id(), feedback);

        FeedbackResponse response =
                FeedbackResponse.from(feedback);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(feedback.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    public void clearMap() {
        feedbacks.clear();
    }

    public void fillMap(Feedback feedback) {
        feedbacks.put(feedback.id(), feedback);
    }

}