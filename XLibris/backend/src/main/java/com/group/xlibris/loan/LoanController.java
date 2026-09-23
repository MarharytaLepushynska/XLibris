package com.group.xlibris.loan;

import com.group.xlibris.loan.dto.LoanRequest;
import com.group.xlibris.loan.dto.LoanResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getById(@PathVariable("id") UUID id) {
        LoanResponse response = loanService.getLoanById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAll(
            @RequestParam(name = "ownerId", required = false) UUID ownerId,
            @RequestParam(name = "renterId", required = false) UUID renterId,
            @RequestParam(name = "loanStatus", required = false) LoanStatus loanStatus
    ) {
        List<LoanResponse> loans = loanService.getAllLoans(ownerId, renterId, loanStatus);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdue() {
        List<LoanResponse> loans = loanService.getAllLoans(null, null, LoanStatus.OVERDUE);
        return ResponseEntity.ok(loans);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest loanRequest) {
        LoanResponse response = loanService.createLoan(loanRequest.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponse> assignToReturned(@PathVariable("id") UUID id) {
        LoanResponse response = loanService.returnLoan(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        loanService.removeLoan(id);
        return ResponseEntity.noContent().build();
    }
}
