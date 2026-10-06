package com.group.xlibris.validator;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;

public interface ValidatorService {

    ProblemDetail createValidationProblem(
            MethodArgumentNotValidException exception
    );
}