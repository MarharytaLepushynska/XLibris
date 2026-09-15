package com.group.xlibris.loan.controller;

import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.loan.dto.LoanRequest;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.loan.enums.LoanStatus;
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
@RequestMapping("/api/loans")
public class LoanController {
    private final Map<UUID, Loan> loans;

    public LoanController() {
        loans = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getById(@PathVariable("id") UUID id) {
        Loan loan = findLoanById(id);
        LoanResponse loanResponse = LoanResponse.from(loan);
        return ResponseEntity.ok(loanResponse);
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAll(
            @RequestParam(name = "ownerId", required = false) UUID ownerId,
            @RequestParam(name = "renterId", required = false) UUID renterId,
            @RequestParam(name = "loanStatus", required = false) LoanStatus loanStatus
    ) {
        List<LoanResponse> responseList = loans.values().stream()
                .filter(loan -> ownerId == null || loan.getOwnerId().equals(ownerId))
                .filter(loan -> renterId == null || loan.getRenterId().equals(renterId))
                .filter(loan -> loanStatus == null || loan.getStatus().equals(loanStatus))
                .map(LoanResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdue() {
        List<LoanResponse> responseList = loans.values().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.OVERDUE)
                .map(LoanResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest loanRequest) {
        Loan loan = new Loan(
                UUID.randomUUID(),
                loanRequest.bookId(),
                loanRequest.ownerId(),
                loanRequest.renterId(),
                Instant.now(),
                loanRequest.expectedReturnDate(),
                null,
                LoanStatus.ACTIVE
        );
        loans.put(loan.getId(), loan);

        LoanResponse loanResponse = LoanResponse.from(loan);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(loanResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(loanResponse);
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponse> assignToReturned(@PathVariable("id") UUID id) {
        Loan loan = findLoanById(id);
        loan.assignToReturned();
        return ResponseEntity.ok(LoanResponse.from(loan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        if (loans.remove(id) == null) {
            throw new NotFoundException("Loan with id " + id + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    public void clearMap() {
        loans.clear();
    }

    public void fillMap(Loan loan) {
        loans.put(loan.getId(), loan);
    }

    private Loan findLoanById(UUID id) {
        Loan loan = loans.get(id);
        if (loan == null) {
            throw new NotFoundException("Loan with id " + id + " not found");
        }
        return loan;
    }
}
