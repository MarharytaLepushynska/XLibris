package com.group.xlibris.notification.repository;

import com.group.xlibris.notification.entity.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findAll();

    void deleteAll();
}