package com.group.xlibris.notification.internal;

import com.group.xlibris.notification.NotificationType;
import com.group.xlibris.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
    }
}