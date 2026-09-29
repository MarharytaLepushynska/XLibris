package com.group.xlibris.feedback.service;

import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.feedback.DuplicateFeedbackException;
import com.group.xlibris.feedback.FeedbackBeforeLoanReturnedException;
import com.group.xlibris.feedback.InvalidFeedbackParticipantsException;
import com.group.xlibris.feedback.SelfFeedbackException;
import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;
import com.group.xlibris.feedback.internal.Feedback;
import com.group.xlibris.feedback.internal.FeedbackRepository;
import com.group.xlibris.feedback.internal.FeedbackServiceImpl;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.loan.LoanStatus;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
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

    @Mock
    private UserService userService;

    private FeedbackServiceImpl feedbackService;

    private UUID feedbackId;
    private UUID loanId;
    private UUID reviewerId;
    private UUID reviewedUserId;

    @BeforeEach
    void setUp() {

        feedbackService = new FeedbackServiceImpl(
                feedbackRepository,
                loanService,
                userService
        );

        feedbackId = UUID.randomUUID();
        loanId = UUID.randomUUID();
        reviewerId = UUID.randomUUID();
        reviewedUserId = UUID.randomUUID();
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

        Loan loan = mock(Loan.class);
        User reviewer = mock(User.class);
        User reviewedUser = mock(User.class);

        when(reviewer.getId()).thenReturn(reviewerId);
        when(reviewedUser.getId()).thenReturn(reviewedUserId);
        when(loan.getId()).thenReturn(loanId);

        when(loanService.getLoanById(loanId))
                .thenReturn(returnedLoan());

        when(feedbackRepository.existsByLoan_IdAndReviewer_Id(
                loanId,
                reviewerId
        )).thenReturn(false);

        when(loanService.getLoanReferenceById(loanId))
                .thenReturn(loan);

        when(userService.getUserReferenceById(reviewerId))
                .thenReturn(reviewer);

        when(userService.getUserReferenceById(reviewedUserId))
                .thenReturn(reviewedUser);

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
                .existsByLoan_IdAndReviewer_Id(
                        loanId,
                        reviewerId
                );

        verify(feedbackRepository)
                .save(any(Feedback.class));

        verify(loanService)
                .getLoanById(loanId);

        verify(loanService)
                .getLoanReferenceById(loanId);

        verify(userService)
                .getUserReferenceById(reviewerId);

        verify(userService)
                .getUserReferenceById(reviewedUserId);
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
        ).existsByLoan_IdAndReviewer_Id(any(), any());

        verify(feedbackRepository, never())
                .save(any());

        verifyNoInteractions(loanService);
        verifyNoInteractions(userService);
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

        when(feedbackRepository.existsByLoan_IdAndReviewer_Id(
                loanId,
                reviewerId
        )).thenReturn(true);

        assertThrows(
                DuplicateFeedbackException.class,
                () -> feedbackService.create(request)
        );

        verify(feedbackRepository)
                .existsByLoan_IdAndReviewer_Id(
                        loanId,
                        reviewerId
                );

        verify(feedbackRepository, never())
                .save(any());

        verify(loanService, never())
                .getLoanReferenceById(any());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldGetFeedbackByIdSuccessfully() {

        Feedback feedback = createFeedback(
                feedbackId,
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience"
        );

        when(feedbackRepository.findByIdWithRelations(feedbackId))
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
                .findByIdWithRelations(feedbackId);
    }

    @Test
    void shouldThrowNotFoundWhenFeedbackMissing() {

        when(feedbackRepository.findByIdWithRelations(feedbackId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> feedbackService.getById(feedbackId)
        );

        verify(feedbackRepository)
                .findByIdWithRelations(feedbackId);
    }

    @Test
    void shouldGetAllFeedbacksSuccessfully() {

        Feedback firstFeedback = createFeedback(
                feedbackId,
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience"
        );

        Feedback secondFeedback = createFeedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                4,
                "Good experience"
        );

        when(feedbackRepository.findAllWithRelations())
                .thenReturn(
                        List.of(
                                firstFeedback,
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

        verify(feedbackRepository)
                .findAllWithRelations();
    }

    @Test
    void shouldFilterFeedbacksByLoanId() {

        Feedback firstFeedback = createFeedback(
                feedbackId,
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience"
        );

        Feedback secondFeedback = createFeedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                "Okay"
        );

        when(feedbackRepository.findAllWithRelations())
                .thenReturn(
                        List.of(
                                firstFeedback,
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

        verify(feedbackRepository)
                .findAllWithRelations();
    }

    @Test
    void shouldFilterFeedbacksByReviewerId() {

        Feedback firstFeedback = createFeedback(
                feedbackId,
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience"
        );

        Feedback secondFeedback = createFeedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                "Okay"
        );

        when(feedbackRepository.findAllWithRelations())
                .thenReturn(
                        List.of(
                                firstFeedback,
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

        verify(feedbackRepository)
                .findAllWithRelations();
    }

    @Test
    void shouldFilterFeedbacksByReviewedUserId() {

        Feedback firstFeedback = createFeedback(
                feedbackId,
                loanId,
                reviewerId,
                reviewedUserId,
                5,
                "Great experience"
        );

        Feedback secondFeedback = createFeedback(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                "Okay"
        );

        when(feedbackRepository.findAllWithRelations())
                .thenReturn(
                        List.of(
                                firstFeedback,
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

        verify(feedbackRepository)
                .findAllWithRelations();
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

        verify(feedbackRepository, never())
                .existsByLoan_IdAndReviewer_Id(any(), any());

        verify(loanService, never())
                .getLoanReferenceById(any());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldAllowOwnerToLeaveFeedbackForRenter() {

        FeedbackRequest request = new FeedbackRequest(
                loanId,
                reviewedUserId,
                reviewerId,
                5,
                "Great renter"
        );

        Loan loan = mock(Loan.class);
        User owner = mock(User.class);
        User renter = mock(User.class);

        when(loan.getId()).thenReturn(loanId);
        when(owner.getId()).thenReturn(reviewedUserId);
        when(renter.getId()).thenReturn(reviewerId);

        when(loanService.getLoanById(loanId))
                .thenReturn(returnedLoan());

        when(feedbackRepository.existsByLoan_IdAndReviewer_Id(
                loanId,
                reviewedUserId
        )).thenReturn(false);

        when(loanService.getLoanReferenceById(loanId))
                .thenReturn(loan);

        when(userService.getUserReferenceById(reviewedUserId))
                .thenReturn(owner);

        when(userService.getUserReferenceById(reviewerId))
                .thenReturn(renter);

        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FeedbackResponse response =
                feedbackService.create(request);

        assertNotNull(response);
        assertEquals(reviewedUserId, response.reviewerId());
        assertEquals(reviewerId, response.reviewedUserId());

        verify(feedbackRepository)
                .save(any(Feedback.class));
    }

    @Test
    void shouldThrowExceptionWhenUsersAreNotLoanParticipants() {

        UUID outsiderId = UUID.randomUUID();

        FeedbackRequest request = new FeedbackRequest(
                loanId,
                outsiderId,
                reviewedUserId,
                5,
                "Feedback"
        );

        when(loanService.getLoanById(loanId))
                .thenReturn(returnedLoan());

        assertThrows(
                InvalidFeedbackParticipantsException.class,
                () -> feedbackService.create(request)
        );

        verify(feedbackRepository, never())
                .existsByLoan_IdAndReviewer_Id(any(), any());

        verify(feedbackRepository, never())
                .save(any());

        verify(loanService, never())
                .getLoanReferenceById(any());

        verifyNoInteractions(userService);
    }

    private Feedback createFeedback(
            UUID id,
            UUID loanId,
            UUID reviewerId,
            UUID reviewedUserId,
            Integer rating,
            String comment
    ) {

        Loan loan = mock(Loan.class);
        User reviewer = mock(User.class);
        User reviewedUser = mock(User.class);

        lenient().when(loan.getId()).thenReturn(loanId);
        lenient().when(reviewer.getId()).thenReturn(reviewerId);
        lenient().when(reviewedUser.getId()).thenReturn(reviewedUserId);

        return new Feedback(
                id,
                loan,
                reviewer,
                reviewedUser,
                rating,
                comment,
                Instant.now()
        );
    }

    private LoanResponse returnedLoan() {

        return new LoanResponse(
                loanId,
                UUID.randomUUID(),
                reviewedUserId,
                reviewerId,
                Instant.now().minusSeconds(86400),
                Instant.now().minusSeconds(3600),
                Instant.now(),
                LoanStatus.RETURNED
        );
    }
}