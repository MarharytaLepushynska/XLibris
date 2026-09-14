package com.group.xlibris.notification.controller;

import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;
import com.group.xlibris.notification.entity.Notification;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final Map<UUID, Notification> notifications;

    public NotificationController() {
        notifications = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(
            @PathVariable UUID id
    ) {

        Notification notification = findNotificationById(id);

        return ResponseEntity.ok(
                NotificationResponse.from(notification)
        );
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @RequestParam(required = false) UUID userId
    ) {

        List<NotificationResponse> responseList =
                notifications.values()
                        .stream()
                        .filter(notification ->
                                userId == null
                                || notification.userId().equals(userId))
                        .map(NotificationResponse::from)
                        .toList();

        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationRequest request
    ) {

        Notification notification = new Notification(
                UUID.randomUUID(),
                request.userId(),
                request.type(),
                request.message(),
                Instant.now()
        );

        notifications.put(
                notification.id(),
                notification
        );

        NotificationResponse response =
                NotificationResponse.from(notification);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(notification.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    private Notification findNotificationById(UUID id) {

        Notification notification = notifications.get(id);

        if (notification == null) {
            throw new NotFoundException(
                    "Notification with id " + id + " not found"
            );
        }

        return notification;
    }
}