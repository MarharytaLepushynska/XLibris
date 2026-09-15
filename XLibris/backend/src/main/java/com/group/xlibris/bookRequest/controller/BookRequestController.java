package com.group.xlibris.bookRequest.controller;

import com.group.xlibris.bookRequest.dto.BookRequestCreate;
import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.bookRequest.dto.BookRequestUpdateStatus;
import com.group.xlibris.bookRequest.entity.BookRequestEntity;
import com.group.xlibris.bookRequest.enums.BookRequestStatus;
import com.group.xlibris.common.exception.NotFoundException;
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
@RequestMapping("/api/book-requests")
public class BookRequestController {
    private final Map<UUID, BookRequestEntity> requests;

    public BookRequestController() {
        this.requests = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookRequestResponse> getById(@PathVariable UUID id) {
        BookRequestEntity entity = requests.get(id);
        if(entity == null) {
            throw new NotFoundException("Request with id " + id + " not found");
        }
        BookRequestResponse response = toResponse(entity);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BookRequestResponse>> getAll(@RequestParam(required = false) UUID bookId,
                                                            @RequestParam(required = false) UUID requesterId,
                                                            @RequestParam(required = false) BookRequestStatus status,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        List<BookRequestResponse> responseList = requests.values().stream()
                .filter(r -> bookId == null || r.getBookId().equals(bookId))
                .filter(r -> requesterId == null || r.getRequesterId().equals(requesterId))
                .filter(r -> status == null || r.getStatus().equals(status))
                .skip((long) page*size)
                .limit(size)
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<BookRequestResponse> create(@Valid @RequestBody BookRequestCreate request,
                                                    @PathVariable UUID bookId) {
        BookRequestEntity entity = createRequest(request, bookId);
        requests.put(entity.getId(), entity);

        BookRequestResponse response = toResponse(entity);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookRequestResponse> updateStatus (@PathVariable UUID id, @Valid @RequestBody BookRequestUpdateStatus request) {
        if(!requests.containsKey(id)) {
            throw new NotFoundException("Request with id " + id + " not found");
        }

        BookRequestEntity existing = requests.get(id);
        existing.setStatus(request.status());
        existing.setRespondedAt(Instant.now());

        requests.put(id, existing);

        BookRequestResponse response = toResponse(existing);
        return  ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        if(!requests.containsKey(id)) {
            throw new NotFoundException("Request with id " + id + " not found");
        }

        requests.remove(id);
        return ResponseEntity.noContent().build();
    }

    private BookRequestEntity createRequest(BookRequestCreate request, UUID bookId) {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        return new BookRequestEntity(
                id,
                bookId,
                request.requesterId(),
                ownerId,
                request.desiredDurationDays(),
                BookRequestStatus.PENDING,
                Instant.now(),
                null
        );
    }

    private BookRequestResponse toResponse(BookRequestEntity entity) {
        return new BookRequestResponse(
             entity.getId(),
             entity.getBookId(),
             entity.getRequesterId(),
             entity.getOwnerId(),
             entity.getDesiredDurationDays(),
             entity.getStatus(),
             entity.getCreatedAt(),
             entity.getRespondedAt()
        );
    }
}
