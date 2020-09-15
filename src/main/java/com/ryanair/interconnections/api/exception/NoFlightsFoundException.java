package com.ryanair.interconnections.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception to throw with not found code (404) if there is no flights found
 */
public class NoFlightsFoundException extends ResponseStatusException {
    public NoFlightsFoundException() {
        super(HttpStatus.NOT_FOUND, "There's no flights found");
    }
}
