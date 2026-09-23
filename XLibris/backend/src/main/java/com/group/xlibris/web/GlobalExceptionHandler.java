package com.group.xlibris.web;

import com.group.xlibris.bookRequest.DuplicateBookRequestException;
import com.group.xlibris.bookRequest.InvalidBookRequestStateException;
import com.group.xlibris.bookRequest.InvalidBookStateException;
import com.group.xlibris.common.AccessDeniedException;
import com.group.xlibris.common.IdMismatch;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.feedback.DuplicateFeedbackException;
import com.group.xlibris.feedback.SelfFeedbackException;
import com.group.xlibris.feedback.FeedbackBeforeLoanReturnedException;
import com.group.xlibris.loan.InvalidLoanStateException;
import com.group.xlibris.loan.InvalidReturnDateException;
import com.group.xlibris.loan.SameParticipantException;
import com.group.xlibris.report.InvalidReportResolutionException;
import com.group.xlibris.report.InvalidReportStateException;
import com.group.xlibris.report.NotLoanParticipantException;
import com.group.xlibris.report.SelfReportException;
import com.group.xlibris.user.ContactAccessDeniedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            NotFoundException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Resource not found");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problem);
    }

    @ExceptionHandler(IdMismatch.class)
    public ResponseEntity<ProblemDetail> handleIdMismatch(
            IdMismatch exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("ID mismatch");

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request contains invalid fields"
        );

        problem.setTitle("Validation failed");

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        problem.setProperty("errors", errors);

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid request body");

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(SelfFeedbackException.class)
    public ResponseEntity<ProblemDetail> handleSelfFeedback(
            SelfFeedbackException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Business rule error");

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(DuplicateFeedbackException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateFeedback(
            DuplicateFeedbackException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Business rule violation");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

    @ExceptionHandler(FeedbackBeforeLoanReturnedException.class)
    public ResponseEntity<ProblemDetail> handleFeedbackBeforeLoanReturned(
            FeedbackBeforeLoanReturnedException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage()
        );

        problem.setTitle("Loan has not been returned");

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());

        pd.setTitle("Business rule error");

        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(
            IllegalStateException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Business rule violation");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

    @ExceptionHandler(ContactAccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleContactAccessDenied(ContactAccessDeniedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );

        problem.setTitle("Access to contact information denied");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );

        problem.setTitle("Access to denied");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problem);
    }

    @ExceptionHandler(InvalidBookRequestStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidBookRequestStateException(InvalidBookRequestStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage()
        );

        problem.setTitle("Invalid status for book request");

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(problem);
    }

    @ExceptionHandler(InvalidBookStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidBookStateException(InvalidBookStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage()
        );

        problem.setTitle("Invalid status for book to borrow");

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(problem);
    }


    @ExceptionHandler(InvalidLoanStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidLoanStateException(InvalidLoanStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage()
        );
        problem.setTitle("Invalid loan state");

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(problem);
    }

    @ExceptionHandler(SameParticipantException.class)
    public ResponseEntity<ProblemDetail> handleSameParticipantException(SameParticipantException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Same participant violation");

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(InvalidReturnDateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidReturnDateException(InvalidReturnDateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Invalid return date");

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(SelfReportException.class)
    public ResponseEntity<ProblemDetail> handleSelfReportException(SelfReportException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Self report violation");

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(NotLoanParticipantException.class)
    public ResponseEntity<ProblemDetail> handleNotLoanParticipantException(NotLoanParticipantException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Not a loan participant");

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(InvalidReportStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidReportStateException(InvalidReportStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage()
        );
        problem.setTitle("Invalid report state transition");

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(problem);
    }

    @ExceptionHandler(InvalidReportResolutionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidReportResolutionException(InvalidReportResolutionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage()
        );
        problem.setTitle("Invalid report resolution");

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(problem);
    }

    @ExceptionHandler(DuplicateBookRequestException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateBookRequestException(DuplicateBookRequestException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problem.setTitle("Book request duplicate");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

}