package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationKYCException extends RuntimeException {
    public ValidationKYCException(String message) {
        super(message);
    }
}
