package com.group.xlibris.feedback.service;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import com.group.xlibris.feedback.entity.Feedback;
import com.group.xlibris.feedback.exception.DuplicateFeedbackException;
import com.group.xlibris.feedback.exception.SelfFeedbackException;
import com.group.xlibris.feedback.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public FeedbackResponse create(FeedbackRequest request) {

        if (request.reviewerId().equals(request.reviewedUserId())) {
            throw new SelfFeedbackException(
                    "User cannot leave feedback for themselves"
            );
        }

        if (feedbackRepository.existsByLoanIdAndReviewerId(
                request.loanId(),
                request.reviewerId()
        )) {
            throw new DuplicateFeedbackException(
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

        return FeedbackResponse.from(
                feedbackRepository.save(feedback)
        );
    }

    @Override
    public FeedbackResponse getById(UUID id) {

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Feedback with id " + id + " not found"
                ));

        return FeedbackResponse.from(feedback);
    }

    @Override
    public List<FeedbackResponse> getAll(
            UUID loanId,
            UUID reviewerId,
            UUID reviewedUserId
    ) {

        return feedbackRepository.findAll()
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
    }
}