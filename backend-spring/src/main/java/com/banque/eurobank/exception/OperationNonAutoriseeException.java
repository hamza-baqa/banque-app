package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OperationNonAutoriseeException extends RuntimeException {
    public OperationNonAutoriseeException(String message) {
        super(message);
    }
}
