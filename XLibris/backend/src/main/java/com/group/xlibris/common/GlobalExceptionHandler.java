package com.group.xlibris.common;

import com.group.xlibris.common.exception.IdMismatch;
import com.group.xlibris.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource not found");
        pd.setType(URI.create("https://xlibris.group.com/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(IdMismatch.class)
    public ProblemDetail handleIdMismatch(IdMismatch ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Id mismatch");
        pd.setType(URI.create("https://xlibris.group.com/errors/id-mismatch"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation exception");

        pd.setTitle("Not appropriate arguments");
        pd.setType(URI.create("https://xlibris.group.com/errors/validation-error"));
        pd.setProperty("timestamp", Instant.now());

        Map<String, String> errors = new HashMap<>();

        for (FieldError error: ex.getBindingResult().getFieldErrors()) {
            String field = error.getField();

            String mess = error.getDefaultMessage() != null ? error.getDefaultMessage() : "Not valid value";

            if(!errors.containsKey(field)) {
                errors.put(field, mess);
            }
        }

        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());

        pd.setTitle("Business rule error");
        pd.setType(URI.create("https://xlibris.group.com/errors/illegal-argument-error"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
