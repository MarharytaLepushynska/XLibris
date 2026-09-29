package com.group.xlibris.notification.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser_Id(UUID userId);

    @Query("""
            SELECT n
            FROM Notification n
            JOIN FETCH n.user
            """)
    List<Notification> findAllWithUser();

    @Query("""
            SELECT n
            FROM Notification n
            JOIN FETCH n.user
            WHERE n.id = :id
            """)
    Optional<Notification> findByIdWithUser(
            @Param("id") UUID id
    );
}