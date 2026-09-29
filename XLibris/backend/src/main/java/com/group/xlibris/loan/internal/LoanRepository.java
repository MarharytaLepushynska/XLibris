package com.group.xlibris.loan.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    @Query("SELECT l FROM Loan l " +
            "JOIN FETCH l.book " +
            "JOIN FETCH l.owner " +
            "JOIN FETCH l.renter " +
            "WHERE (:ownerId IS NULL OR l.owner.id = :ownerId) AND " +
            "(:renterId IS NULL OR l.renter.id = :renterId)")
    List<Loan> findLoansByCriteria(@Param("ownerId") UUID ownerId,
                                   @Param("renterId") UUID renterId);
}
