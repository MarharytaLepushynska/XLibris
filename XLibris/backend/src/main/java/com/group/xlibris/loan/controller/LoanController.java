package com.group.xlibris.loan.controller;

import com.group.xlibris.common.exception.IdMismatch;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import com.group.xlibris.loan.dto.LoanRequest;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.loan.enums.LoanStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<LoanResponse> getById(@PathVariable UUID id) {
        Loan loan = loans.get(id);
        if (loan == null) {
            throw new NotFoundException("Loan with id " + id + " not found");
        }
        LoanResponse loanResponse = toResponse(loan);
        return ResponseEntity.ok(loanResponse);
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAll() {
        List<LoanResponse> responseList = loans.values().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<LoanResponse> create(@Validated(OnCreate.class) @RequestBody LoanRequest loanRequest) {
        Loan loan = createLoan(loanRequest);
        loans.put(loan.id(), loan);

        LoanResponse loanResponse = toResponse(loan);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(loanResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(loanResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> update(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody LoanRequest loanRequest) {
        if (!id.equals(loanRequest.id())) {
            throw new IdMismatch("Id mismatch");
        }

        if (!loans.containsKey(loanRequest.id())) {
            throw new NotFoundException("Loan with id " + id + " not found");
        }

        Loan oldLoan = loans.get(id);
        LoanStatus newStatus = (loanRequest.actualReturnDate() != null)
                ? LoanStatus.RETURNED
                : oldLoan.status();

        Loan updatedLoan = new Loan(
                id,
                loanRequest.bookId(),
                loanRequest.ownerId(),
                loanRequest.renterId(),
                oldLoan.startDate(),
                loanRequest.expectedReturnDate(),
                loanRequest.actualReturnDate(),
                newStatus
        );
        loans.put(id, updatedLoan);

        LoanResponse loanResponse = toResponse(updatedLoan);
        return ResponseEntity.ok(loanResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!loans.containsKey(id)) {
            throw new NotFoundException("Loan with id " + id + " not found");
        }
        loans.remove(id);
        return ResponseEntity.noContent().build();
    }

    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.id(),
                loan.bookId(),
                loan.ownerId(),
                loan.renterId(),
                loan.startDate(),
                loan.expectedReturnDate(),
                loan.actualReturnDate(),
                loan.status());
    }

    private Loan createLoan(LoanRequest loanRequest) {
        UUID loanId = UUID.randomUUID();
        return new Loan(loanId,
                loanRequest.bookId(),
                loanRequest.ownerId(),
                loanRequest.renterId(),
                Instant.now(),
                loanRequest.expectedReturnDate(),
                null,
                LoanStatus.ACTIVE);
    }
}
