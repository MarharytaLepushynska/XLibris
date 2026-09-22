package com.group.xlibris.notification.repository;

import com.group.xlibris.notification.entity.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final Map<UUID, Notification> notifications =
            new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification notification) {
        notifications.put(notification.id(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(notifications.get(id));
    }

    @Override
    public List<Notification> findAll() {
        return List.copyOf(notifications.values());
    }

    @Override
    public void deleteAll() {
        notifications.clear();
    }
}