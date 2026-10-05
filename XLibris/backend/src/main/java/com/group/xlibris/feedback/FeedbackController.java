package com.group.xlibris.feedback;

import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedbacks")
@Tag(name = "Feedbacks after book loan", description = "Endpoints for submitting user feedback after book loans")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feedback by ID", description = "Retrieves detailed information about a specific feedback record by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feedback successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    public ResponseEntity<FeedbackResponse> getById(@PathVariable("id") UUID id) {
        FeedbackResponse response = feedbackService.getById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all feedbacks", description = "Retrieves a filtered list of feedbacks based on loan id, reviewer, or reviewed user criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of feedbacks successfully retrieved")
    })
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
    @Operation(summary = "Create feedback", description = "Submits a rating and comment for a participant after the book loan has been returned")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Feedback successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error, self-feedback, or invalid participants"),
            @ApiResponse(responseCode = "422", description = "Loan has not been returned yet"),
            @ApiResponse(responseCode = "409", description = "Feedback for this loan has already been submitted"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
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