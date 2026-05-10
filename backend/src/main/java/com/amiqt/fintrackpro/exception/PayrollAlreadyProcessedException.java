package com.amiqt.fintrackpro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PayrollAlreadyProcessedException extends RuntimeException {
    public PayrollAlreadyProcessedException(String message) {
        super(message);
    }
}
