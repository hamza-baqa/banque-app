package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CarteBloqueException extends RuntimeException {
    public CarteBloqueException(String message) {
        super(message);
    }
}
