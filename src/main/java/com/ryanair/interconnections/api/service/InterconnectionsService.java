package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.exception.DepartureAfterArrivalException;
import com.ryanair.interconnections.api.exception.NoFlightsFoundException;
import com.ryanair.interconnections.api.exception.NoRoutesFoundException;
import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.route.Route;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstract class with the basic tasks of a Interconnections service
 */
public abstract class InterconnectionsService {
    abstract List<FlightResponse> getInterconnections(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) throws ResponseStatusException;

    /**
     * Verify if the departure time is not later than the arrival time
     * @param departureDateTime the departure time
     * @param arrivalDateTime the arrival time
     * @throws DepartureAfterArrivalException with bad request code (400) if the departure time is later than the arrival time
     */
    protected void checkDepartureArrivalTime(LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) throws DepartureAfterArrivalException {
        if (departureDateTime.isAfter(arrivalDateTime)) {
            throw new DepartureAfterArrivalException();
        }
    }

    /**
     * Verify if there is, at least, one flight in the search
     * @param flightResponseList the list of flights as a result of the search
     * @throws NoFlightsFoundException with not found code (404) if the search does not found any available flight
     */
    protected void checkEmptyFlightList(List<FlightResponse> flightResponseList) throws NoFlightsFoundException{
        if (flightResponseList.isEmpty()) {
            throw new NoFlightsFoundException();
        }
    }

    /**
     * Verify if there is, at least, one route in the search
     * @param directRoute the only direct route
     * @param routes a list of interconnected routes
     * @throws NoRoutesFoundException with not found code (404) if the search does not found any available route
     */
    protected void checkEmptyRoutes(Route directRoute, List<List<Route>> routes) throws NoRoutesFoundException{
        if (directRoute == null && routes.isEmpty()) {
            throw new NoRoutesFoundException();
        }
    }
}
