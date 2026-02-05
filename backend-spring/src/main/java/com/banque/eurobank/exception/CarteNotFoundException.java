package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CarteNotFoundException extends RuntimeException {
    public CarteNotFoundException(String message) {
        super(message);
    }
}
