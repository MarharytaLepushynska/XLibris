package com.group.xlibris.bookRequest.controller;

import com.group.xlibris.bookRequest.dto.BookRequestCreate;
import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.bookRequest.dto.BookRequestUpdateStatus;
import com.group.xlibris.landCommon.BookRequestStatus;
import com.group.xlibris.bookRequest.service.BookRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/book-requests")
@Validated
public class BookRequestController {
    private final BookRequestService service;

    public BookRequestController(BookRequestService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookRequestResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BookRequestResponse>> getAll(@RequestParam(required = false) UUID bookId,
                                                            @RequestParam(required = false) UUID requesterId,
                                                            @RequestParam(required = false) BookRequestStatus status,
                                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                                            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        List<BookRequestResponse> responseList = service.getAll(bookId, requesterId, status, page, size);
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<BookRequestResponse> create(@Valid @RequestBody BookRequestCreate request,
                                                    @PathVariable UUID bookId) {
        BookRequestResponse response = service.create(request.toCommand(bookId));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookRequestResponse> updateStatus (@PathVariable UUID id, @Valid @RequestBody BookRequestUpdateStatus request) {
        return ResponseEntity.ok(service.updateStatus(request.toCommand(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
