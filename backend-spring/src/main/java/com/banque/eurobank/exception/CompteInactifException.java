package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CompteInactifException extends RuntimeException {
    public CompteInactifException(String message) {
        super(message);
    }
}
