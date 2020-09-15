package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

public interface InterconnectionsService {
    List<FlightResponse> getInterconnections(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) throws ResponseStatusException;
}
