package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VirementInvalideException extends RuntimeException {
    public VirementInvalideException(String message) {
        super(message);
    }
}
