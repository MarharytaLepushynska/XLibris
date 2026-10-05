package com.group.xlibris.notification;

import com.group.xlibris.notification.dto.NotificationRequest;
import com.group.xlibris.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieves detailed information about a specific notification by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {

        NotificationResponse response =
                notificationService.getById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieves a list of notifications, optionally filtered by user id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of notifications successfully retrieved")
    })
    public ResponseEntity<List<NotificationResponse>> getAll(@RequestParam(required = false) UUID userId) {

        List<NotificationResponse> responseList =
                notificationService.getAll(userId);

        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    @Operation(summary = "Create notification", description = "Creates and sends a new notification to a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input data")
    })
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {

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