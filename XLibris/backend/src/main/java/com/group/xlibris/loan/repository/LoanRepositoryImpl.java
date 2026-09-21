package com.group.xlibris.loan.repository;

import com.group.xlibris.loan.entity.Loan;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LoanRepositoryImpl implements LoanRepository {
    private final Map<UUID, Loan> loans = new ConcurrentHashMap<>();

    @Override
    public Loan save(Loan loan) {
        loans.put(loan.getId(), loan);
        return loan;
    }

    @Override
    public Optional<Loan> findById(UUID id) {
        return Optional.ofNullable(loans.get(id));
    }

    @Override
    public List<Loan> findAll() {
        return List.copyOf(loans.values());
    }


    @Override
    public List<Loan> findByOwnerAndRenter(UUID ownerId, UUID renterId) {
        return loans.values().stream()
                .filter(loan -> ownerId == null || loan.getOwnerId().equals(ownerId))
                .filter(renter -> renterId == null || renter.getRenterId().equals(renterId))
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return loans.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        loans.remove(id);
    }

    public void deleteAll() {
        loans.clear();
    }
}
