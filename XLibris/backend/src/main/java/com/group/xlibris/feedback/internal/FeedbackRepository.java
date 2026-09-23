package com.group.xlibris.feedback.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository {
    Feedback save(Feedback feedback);
    Optional<Feedback> findById(UUID id);
    List<Feedback> findAll();
    boolean existsByLoanIdAndReviewerId(UUID loanId, UUID reviewerId);
    void deleteAll();
}