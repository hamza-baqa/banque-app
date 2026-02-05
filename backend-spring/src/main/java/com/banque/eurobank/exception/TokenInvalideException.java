package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenInvalideException extends RuntimeException {
    public TokenInvalideException(String message) {
        super(message);
    }
}
