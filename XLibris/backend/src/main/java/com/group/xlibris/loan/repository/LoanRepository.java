package com.group.xlibris.loan.repository;

import com.group.xlibris.loan.entity.Loan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findById(UUID id);
    List<Loan> findAll();
    List<Loan> findByOwnerAndRenter(UUID ownerId, UUID renterId);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    void deleteAll();
}
