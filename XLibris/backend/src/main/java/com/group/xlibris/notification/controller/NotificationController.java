package com.group.xlibris.notification.controller;

import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;
import com.group.xlibris.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(
            @PathVariable UUID id
    ) {

        NotificationResponse response =
                notificationService.getById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @RequestParam(required = false) UUID userId
    ) {

        List<NotificationResponse> responseList =
                notificationService.getAll(userId);

        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationRequest request
    ) {

        NotificationResponse response =
                notificationService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }
}