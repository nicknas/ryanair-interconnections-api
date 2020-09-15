package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.route.Route;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface to build a schedules service
 */
public interface SchedulesService {
    List<FlightResponse> getFlightsForRoutes(List<Route> routes, Route directRoute, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime);
}
