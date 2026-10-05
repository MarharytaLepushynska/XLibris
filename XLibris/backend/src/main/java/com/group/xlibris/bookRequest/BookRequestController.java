package com.group.xlibris.bookRequest;

import com.group.xlibris.bookRequest.dto.BookRequestCreate;
import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.bookRequest.dto.BookRequestUpdateStatus;
import com.group.xlibris.common.BookRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Book requests", description = "Endpoints for managing book borrowing requests")
public class BookRequestController {
    private final BookRequestService service;

    public BookRequestController(BookRequestService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book request by ID", description = "Retrieves detailed information about a specific book request by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book request successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Book request not found")
    })
    public ResponseEntity<BookRequestResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all book requests", description = "Retrieves a paginated and filtered list of book requests by book, requester, or status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of book requests successfully retrieved")
    })
    public ResponseEntity<List<BookRequestResponse>> getAll(@RequestParam(required = false) UUID bookId,
                                                            @RequestParam(required = false) UUID requesterId,
                                                            @RequestParam(required = false) BookRequestStatus status,
                                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                                            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        List<BookRequestResponse> responseList = service.getAll(bookId, requesterId, status, page, size);
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/{bookId}")
    @Operation(summary = "Create book request", description = "Submits a new request to borrow a specific book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book request successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error, duplicate request, or invalid book state")
    })
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
    @Operation(summary = "Update book request status", description = "Transitions the status of an existing book request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book request status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid state transition, access denied, or invalid book state"),
            @ApiResponse(responseCode = "404", description = "Book request not found")
    })
    public ResponseEntity<BookRequestResponse> updateStatus (@PathVariable UUID id, @Valid @RequestBody BookRequestUpdateStatus request) {
        return ResponseEntity.ok(service.updateStatus(request.toCommand(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book request", description = "Removes a book request from the system by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book request successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Book request not found")
    })
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
