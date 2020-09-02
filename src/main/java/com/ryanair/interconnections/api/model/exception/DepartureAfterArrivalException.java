package com.ryanair.interconnections.api.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception to throw with bad request code (400) if the departure time is later than the arrival time
 */
public class DepartureAfterArrivalException extends ResponseStatusException {
    public DepartureAfterArrivalException() {
        super(HttpStatus.BAD_REQUEST, "Departure time could not be later than arrival");
    }
}
