package com.group.xlibris.feedback.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    boolean existsByLoan_IdAndReviewer_Id(UUID loanId, UUID reviewerId);

    List<Feedback> findByReviewedUser_Id(UUID reviewedUserId);

    @Query("""
            SELECT f
            FROM Feedback f
            JOIN FETCH f.loan
            JOIN FETCH f.reviewer
            JOIN FETCH f.reviewedUser
            """)
    List<Feedback> findAllWithRelations();

    @Query("""
            SELECT f
            FROM Feedback f
            JOIN FETCH f.loan
            JOIN FETCH f.reviewer
            JOIN FETCH f.reviewedUser
            WHERE f.id = :id
            """)
    Optional<Feedback> findByIdWithRelations(@Param("id") UUID id);
}