package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.client.SchedulesClient;
import com.ryanair.interconnections.api.model.response.FlightLegResponse;
import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.api.model.schedule.Day;
import com.ryanair.interconnections.api.model.schedule.Flight;
import com.ryanair.interconnections.api.model.schedule.Schedule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to build a schedules service
 */
public abstract class SchedulesService {
    protected final SchedulesClient schedulesClient;

    /**
     * Check if the direct flight is not earlier that the departure time and not later that the arrival time
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the flight
     * @param arrivalDateTimeAux the arrival time of the flight
     * @return a boolean result that determines if the direct flight meets with the departure and arrival time
     */
    protected boolean isValidDirectRouteSchedule(LocalDateTime departureDateTime,
                                               LocalDateTime arrivalDateTime,
                                               LocalDateTime departureDateTimeAux,
                                               LocalDateTime arrivalDateTimeAux) {

        return !departureDateTimeAux.isBefore(departureDateTime) && !arrivalDateTimeAux.isAfter(arrivalDateTime);
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
        List<FlightResponse> flightResponseList = new ArrayList<>();

        // First of all, get the only schedule of the direct route
        Schedule directSchedule = schedulesClient.getSchedule(route, departureDateTimeAux);

        directSchedule.getDays()
                .forEach(day -> day.getFlights()
                        .forEach(flight -> addDirectFlightToList(
                                flightResponseList,
                                route,
                                departureDateTime,
                                arrivalDateTime,
                                departureDateTimeAux,
                                day,
                                flight)));

        return flightResponseList;
    }

    /**
     * Add a direct flight to the flight response list, if meets with the requirements
     * @param flightResponseList the list of flight response
     * @param route a direct route
     * @param departureDateTime the departure time
     * @param arrivalDateTime the arrival time
     * @param departureDateTimeAux the departure time of the flight to add
     * @param day a day object that represents the day of the flight
     * @param flight a flight object that represents the flight to add
     */
    protected void addDirectFlightToList(List<FlightResponse> flightResponseList,
                                       Route route,
                                       LocalDateTime departureDateTime,
                                       LocalDateTime arrivalDateTime,
                                       LocalDateTime departureDateTimeAux,
                                       Day day,
                                       Flight flight) {
        FlightLegResponse directFlightResponse = new FlightLegResponse(route.getAirportFrom(),
                route.getAirportTo(),
                departureDateTimeAux.withDayOfMonth(day.getDay()).with(flight.getDepartureTime()),
                departureDateTimeAux.withDayOfMonth(day.getDay()).with(flight.getArrivalTime()));

        if (isValidDirectRouteSchedule(
                departureDateTime,
                arrivalDateTime,
                directFlightResponse.getDepartureDateTime(),
                directFlightResponse.getArrivalDateTime())) {

            FlightResponse flightResponse = new FlightResponse();
            flightResponse.setStops(0);
            List<FlightLegResponse> flightLegResponseList = new ArrayList<>();
            flightLegResponseList.add(directFlightResponse);
            flightResponse.setLegs(flightLegResponseList);
            flightResponseList.add(flightResponse);
        }
    }

    public SchedulesService(SchedulesClient schedulesClient) {
        this.schedulesClient = schedulesClient;
    }

    abstract List<FlightResponse> getFlightsForRoutes(List<Route> routes, Route directRoute, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime);
}
