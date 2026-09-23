package com.group.xlibris.feedback.service;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import com.group.xlibris.feedback.internal.Feedback;
import com.group.xlibris.feedback.DuplicateFeedbackException;
import com.group.xlibris.feedback.SelfFeedbackException;
import com.group.xlibris.feedback.FeedbackBeforeLoanReturnedException;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.LoanStatus;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.feedback.internal.FeedbackRepository;
import com.group.xlibris.feedback.internal.FeedbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private LoanService loanService;

    private FeedbackServiceImpl feedbackService;

    private UUID feedbackId;
    private UUID loanId;
    private UUID reviewerId;
    private UUID reviewedUserId;
    private Feedback feedback;

    @BeforeEach
    void setUp() {

        feedbackService = new FeedbackServiceImpl(feedbackRepository, loanService);

        feedbackId = UUID.randomUUID();
        loanId = UUID.randomUUID();
        reviewerId = UUID.randomUUID();
        reviewedUserId = UUID.randomUUID();

        feedback = new Feedback(
                feedbackId,
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience",
                Instant.now()
        );
    }

    @Test
    void shouldCreateFeedbackSuccessfully() {

        FeedbackRequest request = new FeedbackRequest(
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience"
        );

        when(loanService.getLoanById(loanId))
                .thenReturn(returnedLoan());

        when(feedbackRepository.existsByLoanIdAndReviewerId(
                loanId,
                reviewerId
        )).thenReturn(false);

        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FeedbackResponse response =
                feedbackService.create(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(loanId, response.loanId());
        assertEquals(reviewerId, response.reviewerId());
        assertEquals(reviewedUserId, response.reviewedUserId());
        assertEquals(5, response.rating());
        assertEquals("Great experience", response.comment());
        assertNotNull(response.createdAt());

        verify(feedbackRepository)
                .existsByLoanIdAndReviewerId(
                        loanId,
                        reviewerId
                );

        verify(feedbackRepository)
                .save(any(Feedback.class));

        verify(loanService).getLoanById(loanId);
    }

    @Test
    void shouldThrowSelfFeedbackException() {

        FeedbackRequest request = new FeedbackRequest(
                loanId,
                reviewerId,
                reviewerId,
                5,
                "My own feedback"
        );

        assertThrows(
                SelfFeedbackException.class,
                () -> feedbackService.create(request)
        );

        verify(
                feedbackRepository,
                never()
        ).existsByLoanIdAndReviewerId(any(), any());

        verify(feedbackRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowDuplicateFeedbackException() {

        FeedbackRequest request = new FeedbackRequest(
                loanId,
                reviewerId,
                reviewedUserId,
                4,
                "Second feedback"
        );

        when(loanService.getLoanById(loanId))
                .thenReturn(returnedLoan());

        when(feedbackRepository.existsByLoanIdAndReviewerId(
                loanId,
                reviewerId
        )).thenReturn(true);

        assertThrows(
                DuplicateFeedbackException.class,
                () -> feedbackService.create(request)
        );

        verify(feedbackRepository)
                .existsByLoanIdAndReviewerId(
                        loanId,
                        reviewerId
                );

        verify(feedbackRepository, never())
                .save(any());
    }

    @Test
    void shouldGetFeedbackByIdSuccessfully() {

        when(feedbackRepository.findById(feedbackId))
                .thenReturn(Optional.of(feedback));

        FeedbackResponse response =
                feedbackService.getById(feedbackId);

        assertNotNull(response);
        assertEquals(feedbackId, response.id());
        assertEquals(loanId, response.loanId());
        assertEquals(reviewerId, response.reviewerId());
        assertEquals(reviewedUserId, response.reviewedUserId());
        assertEquals(5, response.rating());
        assertEquals("Great experience", response.comment());

        verify(feedbackRepository)
                .findById(feedbackId);
    }

    @Test
    void shouldThrowNotFoundWhenFeedbackMissing() {

        when(feedbackRepository.findById(feedbackId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> feedbackService.getById(feedbackId)
        );

        verify(feedbackRepository)
                .findById(feedbackId);
    }

    @Test
    void shouldGetAllFeedbacksSuccessfully() {

        Feedback secondFeedback = new Feedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                4,
                "Good experience",
                Instant.now()
        );

        when(feedbackRepository.findAll())
                .thenReturn(
                        List.of(
                                feedback,
                                secondFeedback
                        )
                );

        List<FeedbackResponse> responses =
                feedbackService.getAll(
                        null,
                        null,
                        null
                );

        assertEquals(2, responses.size());

        verify(feedbackRepository).findAll();
    }

    @Test
    void shouldFilterFeedbacksByLoanId() {

        Feedback secondFeedback = new Feedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                "Okay",
                Instant.now()
        );

        when(feedbackRepository.findAll())
                .thenReturn(
                        List.of(
                                feedback,
                                secondFeedback
                        )
                );

        List<FeedbackResponse> responses =
                feedbackService.getAll(
                        loanId,
                        null,
                        null
                );

        assertEquals(1, responses.size());
        assertEquals(
                loanId,
                responses.getFirst().loanId()
        );

        verify(feedbackRepository).findAll();
    }

    @Test
    void shouldFilterFeedbacksByReviewerId() {

        Feedback secondFeedback = new Feedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                "Okay",
                Instant.now()
        );

        when(feedbackRepository.findAll())
                .thenReturn(
                        List.of(
                                feedback,
                                secondFeedback
                        )
                );

        List<FeedbackResponse> responses =
                feedbackService.getAll(
                        null,
                        reviewerId,
                        null
                );

        assertEquals(1, responses.size());
        assertEquals(
                reviewerId,
                responses.getFirst().reviewerId()
        );

        verify(feedbackRepository).findAll();
    }

    @Test
    void shouldFilterFeedbacksByReviewedUserId() {

        Feedback secondFeedback = new Feedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                "Okay",
                Instant.now()
        );

        when(feedbackRepository.findAll())
                .thenReturn(
                        List.of(
                                feedback,
                                secondFeedback
                        )
                );

        List<FeedbackResponse> responses =
                feedbackService.getAll(
                        null,
                        null,
                        reviewedUserId
                );

        assertEquals(1, responses.size());
        assertEquals(
                reviewedUserId,
                responses.getFirst().reviewedUserId()
        );

        verify(feedbackRepository).findAll();
    }

    @Test
    void shouldThrowExceptionWhenLoanIsNotReturned() {

        FeedbackRequest request = new FeedbackRequest(
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience"
        );

        LoanResponse activeLoan = new LoanResponse(
                loanId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                Instant.now().plusSeconds(86400),
                null,
                LoanStatus.ACTIVE
        );

        when(loanService.getLoanById(loanId))
                .thenReturn(activeLoan);

        assertThrows(
                FeedbackBeforeLoanReturnedException.class,
                () -> feedbackService.create(request)
        );

        verify(feedbackRepository, never())
                .save(any());
    }

    private LoanResponse returnedLoan() {
        return new LoanResponse(
                loanId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now().minusSeconds(86400),
                Instant.now().minusSeconds(3600),
                Instant.now(),
                LoanStatus.RETURNED
        );
    }
}