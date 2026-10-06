package com.group.xlibris.validator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultValidatorService implements ValidatorService {

    private final ValidatorProperties properties;

    public DefaultValidatorService(ValidatorProperties properties) {
        this.properties = properties;
    }

    @Override
    public ProblemDetail createValidationProblem(
            MethodArgumentNotValidException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                properties.detail()
        );

        problem.setTitle(properties.title());

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        problem.setProperty("errors", errors);

        return problem;
    }
}