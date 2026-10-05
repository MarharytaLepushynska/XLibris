package com.group.xlibris.loan.dto;

import com.group.xlibris.loan.internal.Loan;
import com.group.xlibris.loan.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.modulith.NamedInterface;

import java.time.Instant;
import java.util.UUID;

@NamedInterface(value = "api")
@Schema(description = "Detailed representation of a book loan response")
public record LoanResponse(
        @Schema(description = "Unique id of the loan", example = "550e8400-e29b-41d4-a716-446655444000")
        UUID id,

        @Schema(description = "Unique id of the loaned book", example = "550e8400-e29b-41d4-a716-446655445000")
        UUID bookId,

        @Schema(description = "Unique id of the book owner", example = "550e8400-e29b-41d4-a716-446655446000")
        UUID ownerId,

        @Schema(description = "Unique id of the renter", example = "550e8400-e29b-41d4-a716-446655447000")
        UUID renterId,

        @Schema(description = "Timestamp when the loan was initially created", example = "2026-10-05T10:00:00Z")
        Instant startDate,

        @Schema(description = "Agreed expected return timestamp", example = "2026-11-01T12:00:00Z")
        Instant expectedReturnDate,

        @Schema(description = "Actual return timestamp (null if not returned yet)", example = "2026-10-25T14:30:00Z")
        Instant actualReturnDate,

        @Schema(description = "Current status of the loan", example = "ACTIVE")
        LoanStatus status
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getOwner().getId(),
                loan.getRenter().getId(),
                loan.getStartDate(),
                loan.getExpectedReturnDate(),
                loan.getActualReturnDate(),
                loan.getStatus()
        );
    }
}
