package com.group.xlibris.feedback;

import com.group.xlibris.feedback.dto.FeedbackRequest;
import com.group.xlibris.feedback.dto.FeedbackResponse;

import java.util.List;
import java.util.UUID;

public interface FeedbackService {

    FeedbackResponse create(FeedbackRequest request);

    FeedbackResponse getById(UUID id);

    List<FeedbackResponse> getAll(
            UUID loanId,
            UUID reviewerId,
            UUID reviewedUserId
    );
}