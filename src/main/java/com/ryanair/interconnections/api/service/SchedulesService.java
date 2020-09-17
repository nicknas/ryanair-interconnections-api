package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.client.SchedulesClient;
import com.ryanair.interconnections.api.model.response.FlightLegResponse;
import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.api.model.schedule.Schedule;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface to build a schedules service
 */
public abstract class SchedulesService {
    protected final SchedulesClient schedulesClient;

    /**
     * Check if the direct flight is not earlier that the departure time and not later that the arrival time
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param flightDepartureDateTime the departure time of the flight
     * @param flightArrivalDateTime the arrival time of the flight
     * @return a boolean result that determines if the direct flight meets with the departure and arrival time
     */
    protected boolean isValidDirectFlight(LocalDateTime departureDateTime,
                                               LocalDateTime arrivalDateTime,
                                               LocalDateTime flightDepartureDateTime,
                                               LocalDateTime flightArrivalDateTime) {

        return !flightDepartureDateTime.isBefore(departureDateTime) && !flightArrivalDateTime.isAfter(arrivalDateTime);
    }

    /**
     * Make a List of all the direct flights to store in the interconnections response
     * @param route the direct route
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the direct flight
     * @return a List of all the direct flights
     */
    protected List<FlightResponse> getDirectRouteFlights(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, LocalDateTime departureDateTimeAux) {
        // First of all, get the only schedule of the direct route
        Schedule directSchedule = schedulesClient.getSchedule(route, departureDateTimeAux);

        return directSchedule.getDays()
                .stream()
                .flatMap(day -> day.getFlights()
                .stream()
                .map(flight -> new FlightResponse(0, Collections.singletonList(new FlightLegResponse(route.getAirportFrom(),
                        route.getAirportTo(),
                        departureDateTimeAux.withDayOfMonth(day.getDay()).with(flight.getDepartureTime()),
                        departureDateTimeAux.withDayOfMonth(day.getDay()).with(flight.getArrivalTime()))))))
                .filter(flightResponse -> isValidDirectFlight(
                        departureDateTime,
                        arrivalDateTime,
                        flightResponse.getLegs().get(0).getDepartureDateTime(),
                        flightResponse.getLegs().get(0).getArrivalDateTime()))
                .collect(Collectors.toList());

    }

    public SchedulesService(SchedulesClient schedulesClient) {
        this.schedulesClient = schedulesClient;
    }

    abstract List<FlightResponse> getFlightsForRoutes(List<List<Route>> routes, Route directRoute, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime);
}
