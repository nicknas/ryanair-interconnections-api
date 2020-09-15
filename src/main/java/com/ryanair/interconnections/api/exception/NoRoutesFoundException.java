package com.ryanair.interconnections.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception to throw with not found code (404) if there is no routes found
 */
public class NoRoutesFoundException extends ResponseStatusException {
    public NoRoutesFoundException() {
        super(HttpStatus.NOT_FOUND, "There's no routes found");
    }
}
