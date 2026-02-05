package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CompteBloqueException extends RuntimeException {
    public CompteBloqueException(String message) {
        super(message);
    }
}
