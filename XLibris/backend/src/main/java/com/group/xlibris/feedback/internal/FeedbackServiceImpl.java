package com.group.xlibris.feedback.internal;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.feedback.FeedbackService;
import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import com.group.xlibris.feedback.DuplicateFeedbackException;
import com.group.xlibris.feedback.SelfFeedbackException;
import com.group.xlibris.feedback.FeedbackBeforeLoanReturnedException;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.LoanStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final LoanService loanService;

    public FeedbackServiceImpl(
            FeedbackRepository feedbackRepository,
            LoanService loanService
    ) {
        this.feedbackRepository = feedbackRepository;
        this.loanService = loanService;
    }

    @Override
    public FeedbackResponse create(FeedbackRequest request) {

        if (request.reviewerId().equals(request.reviewedUserId())) {
            throw new SelfFeedbackException(
                    "User cannot leave feedback for themselves"
            );
        }

        var loan = loanService.getLoanById(request.loanId());

        if (loan.status() != LoanStatus.RETURNED) {
            throw new FeedbackBeforeLoanReturnedException(
                    "Feedback can only be submitted after the loan is returned"
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

        Feedback savedFeedback = feedbackRepository.save(feedback);
        System.out.println("Feedback with id " + savedFeedback.id() + " was created");
        return FeedbackResponse.from(savedFeedback);
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