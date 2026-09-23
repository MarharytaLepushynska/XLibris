package com.group.xlibris.feedback.internal;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class FeedbackRepositoryImpl implements FeedbackRepository {

    private final Map<UUID, Feedback> feedbacks =
            new ConcurrentHashMap<>();

    @Override
    public Feedback save(Feedback feedback) {
        feedbacks.put(feedback.id(), feedback);
        return feedback;
    }

    @Override
    public Optional<Feedback> findById(UUID id) {
        return Optional.ofNullable(feedbacks.get(id));
    }

    @Override
    public List<Feedback> findAll() {
        return List.copyOf(feedbacks.values());
    }

    @Override
    public boolean existsByLoanIdAndReviewerId(
            UUID loanId,
            UUID reviewerId
    ) {
        return feedbacks.values()
                .stream()
                .anyMatch(feedback ->
                        feedback.loanId().equals(loanId)
                        && feedback.reviewerId().equals(reviewerId)
                );
    }

    @Override
    public void deleteAll() {
        feedbacks.clear();
    }
}