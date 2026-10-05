package com.group.xlibris.loan;

import com.group.xlibris.loan.dto.LoanRequest;
import com.group.xlibris.loan.dto.LoanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Book loans", description = "Endpoints for managing book loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book loan by ID", description = "Retrieves detailed information about a specific book loan by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book loan successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Book loan not found")
    })
    public ResponseEntity<LoanResponse> getById(@PathVariable("id") UUID id) {
        LoanResponse response = loanService.getLoanById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all book loans", description = "Retrieves a list of book loans with optional filtering by owner, renter or loan status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of book loans successfully retrieved")
    })
    public ResponseEntity<List<LoanResponse>> getAll(
            @RequestParam(name = "ownerId", required = false) UUID ownerId,
            @RequestParam(name = "renterId", required = false) UUID renterId,
            @RequestParam(name = "loanStatus", required = false) LoanStatus loanStatus
    ) {
        List<LoanResponse> loans = loanService.getAllLoans(ownerId, renterId, loanStatus);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue book loans", description = "Retrieves all active book loans that have passed their expected return date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overdue book loans successfully retrieved")
    })
    public ResponseEntity<List<LoanResponse>> getOverdue() {
        List<LoanResponse> loans = loanService.getAllLoans(null, null, LoanStatus.OVERDUE);
        return ResponseEntity.ok(loans);
    }

    @PostMapping(produces = "application/json")
    @Operation(summary = "Create a loan for book", description = "Registers a new book loan between a book owner and a renter")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book loan successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation failed, invalid return date or same participant of loan for renter and owner")
    })
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest loanRequest) {
        LoanResponse response = loanService.createLoan(loanRequest.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}/return")
    @Operation(summary = "Return a loaned book", description = "Marks an active book loan as returned and updates its status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book successfully marked as returned"),
            @ApiResponse(responseCode = "404", description = "Book loan not found"),
            @ApiResponse(responseCode = "422", description = "Invalid loan state, loan was already returned")
    })
    public ResponseEntity<LoanResponse> assignToReturned(@PathVariable("id") UUID id) {
        LoanResponse response = loanService.returnLoan(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book loan", description = "Removes a book loan record from by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book loan successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Book loan not found")
    })
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        loanService.removeLoan(id);
        return ResponseEntity.noContent().build();
    }
}
