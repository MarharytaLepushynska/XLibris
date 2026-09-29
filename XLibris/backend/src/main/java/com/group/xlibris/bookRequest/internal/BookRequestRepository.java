package com.group.xlibris.bookRequest.internal;

import com.group.xlibris.common.BookRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookRequestRepository extends JpaRepository<BookRequestEntity, UUID> {
    @Query("""
            SELECT r FROM BookRequestEntity r
            JOIN FETCH r.book
            JOIN FETCH r.requester
            JOIN FETCH r.owner
            WHERE (:bookId is null or r.book.id = :bookId)
            AND (:requesterId is null or r.requester.id = :requesterId)
            AND (:status is null or r.status = :status)
            """)
    List<BookRequestEntity> findWithDetails(@Param("bookId") UUID bookId,
                                            @Param("requesterId") UUID requesterId,
                                            @Param("status")BookRequestStatus status,
                                            Pageable pageable);

    List<BookRequestEntity> findByBook_IdAndStatusInOrderByCreatedAtAsc(UUID bookId, Collection<BookRequestStatus> statuses);
    boolean existsByBook_IdAndRequester_IdAndStatusIn(UUID bookId, UUID requesterId, Collection<BookRequestStatus> statuses);
}
