package com.group.xlibris.bookWaitlist.controller;

import com.group.xlibris.bookRequest.entity.BookRequestEntity;
import com.group.xlibris.bookWaitlist.dto.BookWaitlistRequest;
import com.group.xlibris.bookWaitlist.dto.BookWaitlistResponse;
import com.group.xlibris.bookWaitlist.dto.BookWaitlistStatusUpdate;
import com.group.xlibris.bookWaitlist.entity.BookWaitlistEntity;
import com.group.xlibris.bookWaitlist.enums.BookWaitlistStatus;
import com.group.xlibris.common.exception.IdMismatch;
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
@RequestMapping("/api")
public class BookWaitlistController {
    private final Map<UUID, BookWaitlistEntity> waitlist;

    public BookWaitlistController() {
        waitlist = new HashMap<>();
    }

    @GetMapping("/books/{bookId}/waitlist")
    public ResponseEntity<List<BookWaitlistResponse>> getById(@PathVariable UUID bookId) {
        List<BookWaitlistResponse> waitlistForBook = waitlist.values().stream()
                .filter(w -> w.getBookId().equals(bookId))
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(waitlistForBook);
    }

    @PostMapping("/books/{bookId}/waitlist")
    public ResponseEntity<BookWaitlistResponse> create(@PathVariable UUID bookId,
                                                       @Valid @RequestBody BookWaitlistRequest request) {
      if(!bookId.equals(request.bookId())) {
          throw new IdMismatch("Id mismatch");
      }

      int position = (int) (waitlist.values().stream()
                    .filter(w -> w.getBookId().equals(bookId) && w.getStatus() == BookWaitlistStatus.WAITING)
                    .count() + 1);

        BookWaitlistEntity entity = new BookWaitlistEntity(
                UUID.randomUUID(), bookId, request.userId(), position,
                Instant.now(), BookWaitlistStatus.WAITING, null, null
        );

        waitlist.put(entity.getId(), entity);

        BookWaitlistResponse response = toResponse(entity);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/book-waitlist/{id}")
    public ResponseEntity<BookWaitlistResponse> updateStatus(@PathVariable UUID id,
                                                             @RequestBody BookWaitlistStatusUpdate request) {
        if(!waitlist.containsKey(id)) {
            throw new NotFoundException("No request in waitlist with id " + id + " not found");
        }

        BookWaitlistEntity existing = waitlist.get(id);
        existing.setStatus(request.staus());
        waitlist.put(id, existing);

        BookWaitlistResponse response = toResponse(existing);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/book-waitlist/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        if(!waitlist.containsKey(id)) {
            throw new NotFoundException("No request with id " + id + " not found");
        }

        waitlist.remove(id);
        return ResponseEntity.noContent().build();
    }

    private BookWaitlistResponse toResponse(BookWaitlistEntity entity) {
        return new BookWaitlistResponse(
                entity.getId(),
                entity.getBookId(),
                entity.getUserId(),
                entity.getPosition(),
                entity.getJoinedAt(),
                entity.getStatus(),
                entity.getNotifiedAt(),
                entity.getResponseDeadline()
        );
    }
}
