package com.group.xlibris.feedback.internal;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.feedback.DuplicateFeedbackException;
import com.group.xlibris.feedback.FeedbackBeforeLoanReturnedException;
import com.group.xlibris.feedback.FeedbackService;
import com.group.xlibris.feedback.InvalidFeedbackParticipantsException;
import com.group.xlibris.feedback.SelfFeedbackException;
import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.LoanStatus;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final LoanService loanService;
    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    public FeedbackServiceImpl(
            FeedbackRepository feedbackRepository,
            LoanService loanService,
            UserService userService
    ) {
        this.feedbackRepository = feedbackRepository;
        this.loanService = loanService;
        this.userService = userService;
    }

    @Override
    public FeedbackResponse create(FeedbackRequest request) {

        if (request.reviewerId().equals(request.reviewedUserId())) {
            throw new SelfFeedbackException(
                    "User cannot leave feedback for themselves"
            );
        }

        var loanResponse = loanService.getLoanById(request.loanId());

        if (loanResponse.status() != LoanStatus.RETURNED) {
            throw new FeedbackBeforeLoanReturnedException(
                    "Feedback can only be submitted after the loan is returned"
            );
        }

        boolean ownerReviewsRenter =
                request.reviewerId().equals(loanResponse.ownerId())
                && request.reviewedUserId().equals(loanResponse.renterId());

        boolean renterReviewsOwner =
                request.reviewerId().equals(loanResponse.renterId())
                && request.reviewedUserId().equals(loanResponse.ownerId());

        if (!ownerReviewsRenter && !renterReviewsOwner) {
            throw new InvalidFeedbackParticipantsException(
                    "Feedback can only be submitted between participants of the loan"
            );
        }

        if (feedbackRepository.existsByLoan_IdAndReviewer_Id(
                request.loanId(),
                request.reviewerId()
        )) {
            throw new DuplicateFeedbackException(
                    "Feedback for this loan has already been submitted"
            );
        }

        Loan loan = loanService.getLoanReferenceById(request.loanId());
        User reviewer = userService.getUserReferenceById(request.reviewerId());
        User reviewedUser = userService.getUserReferenceById(request.reviewedUserId());

        Feedback feedback = new Feedback(
                UUID.randomUUID(),
                loan,
                reviewer,
                reviewedUser,
                request.rating(),
                request.comment(),
                Instant.now()
        );

        Feedback savedFeedback = feedbackRepository.save(feedback);

        log.info("Feedback with id={} was created", savedFeedback.getId());

        return FeedbackResponse.from(savedFeedback);
    }

    @Override
    public FeedbackResponse getById(UUID id) {

        log.debug("Finding feedback by id={}", id);

        Feedback feedback = feedbackRepository.findByIdWithRelations(id)
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

        log.debug("Fetching all feedbacks with loanId={}, reviewerId={}, reviewedUserId={}", loanId, reviewerId, reviewedUserId);

        return feedbackRepository.findAllWithRelations()
                .stream()
                .filter(feedback ->
                        loanId == null
                        || feedback.getLoan().getId().equals(loanId))
                .filter(feedback ->
                        reviewerId == null
                        || feedback.getReviewer().getId().equals(reviewerId))
                .filter(feedback ->
                        reviewedUserId == null
                        || feedback.getReviewedUser().getId().equals(reviewedUserId))
                .map(FeedbackResponse::from)
                .toList();
    }
}