package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.model.airport.Airport;
import com.ryanair.interconnections.api.model.exception.DepartureAfterArrivalException;
import com.ryanair.interconnections.api.model.exception.NoFlightsFoundException;
import com.ryanair.interconnections.api.model.exception.NoRoutesFoundException;
import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.response.ScheduleResponse;
import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.api.model.schedule.Day;
import com.ryanair.interconnections.api.model.schedule.Flight;
import com.ryanair.interconnections.api.model.schedule.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Service class to do all the operations necessary to search the direct and interconnected flights
 */
@Service
public class InterconnectionsService {

    private static final String routeUrl;
    private static final String scheduleUrl;
    private static final String ryanairLiteral;
    private final RestTemplate restTemplate;
    private final HashMap<String, Airport> airportMap;

    static {
        routeUrl = "https://services-api.ryanair.com/locate/3/routes";
        scheduleUrl = "https://services-api.ryanair.com/timtbl/3/schedules";
        ryanairLiteral = "RYANAIR";
    }

    /**
     * Make a REST API request to Routes API and stores the result as a list of routes
     * @return the list of routes
     */
    private List<Route> getRoutes() {
        Route[] routes = restTemplate.getForObject(routeUrl, Route[].class);
        return new ArrayList<>(Arrays.asList(routes));
    }

    /**
     * Make a REST API request to Schedules API and stores the result as a Schedule object
     * @param route the route that contains departure and arrival airport
     * @param dateTime time to search the schedules
     * @return a Schedule object representing the result of the Schedules API
     */
    private Schedule getSchedule(Route route, LocalDateTime dateTime) {
        return restTemplate.getForObject(
                String.format("%s/%s/%s/years/%s/months/%s",
                        scheduleUrl,
                        route.getAirportFrom(),
                        route.getAirportTo(),
                        dateTime.getYear(),
                        dateTime.getMonthValue()),
                Schedule.class);
    }

    /**
     * Filter if a route has connectingAirport to null and the operator of the route is Ryanair
     * @param connectingAirport the connecting airport of a route
     * @param operator the operator of a route
     * @return a boolean result determining if a route is valid
     */
    private boolean filterValidRoutes(String connectingAirport, String operator) {
        return connectingAirport == null && operator.equals(ryanairLiteral);
    }

    /**
     * Filter if a route is a direct flight
     * @param route the route to check if is a direct flight
     * @param departure the departure airport IATA code to check
     * @param arrival the arrival airport IATA code to check
     * @return a boolean result determining if a route comes from a direct flight
     */
    private boolean filterDirectRoute(Route route, String departure, String arrival) {
        return route.getAirportFrom().equals(departure) && route.getAirportTo().equals(arrival);
    }

    /**
     * Get all the interconnected routes between a departure and an arrival airport
     * @param validRoutes a list of routes that meets all the requirements to be valid for the search
     * @param departure the departure airport IATA code
     * @param arrival the arrival airport IATA code
     * @return a list of all the interconnected routes
     */
    private List<Route> getInterconnectedRoutes(List<Route> validRoutes, String departure, String arrival) {
        // Search all the routes to store each route to the airports map
        for (Route validRoute : validRoutes) {
            // if the airport exists in the map, just add a new route
            if (airportMap.containsKey(validRoute.getAirportFrom())) {
                airportMap.get(validRoute.getAirportFrom()).addRoute(validRoute);
            }
            // create a new airport with the first route
            else {
                airportMap.put(validRoute.getAirportFrom(), new Airport(validRoute));
            }
        }

        return airportMap.get(departure).findInterconnectedRoutes(airportMap, arrival);
    }

    /**
     * Verify if the departure time is not later than the arrival time
     * @param departureDateTime the departure time
     * @param arrivalDateTime the arrival time
     * @throws DepartureAfterArrivalException with bad request code (400) if the departure time is later than the arrival time
     */
    private void checkDepartureArrivalTime(LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) throws DepartureAfterArrivalException {
        if (departureDateTime.isAfter(arrivalDateTime)) {
            throw new DepartureAfterArrivalException();
        }
    }

    /**
     * Verify if there is, at least, one flight in the search
     * @param flightResponseList the list of flights as a result of the search
     * @throws NoFlightsFoundException with not found code (404) if the search does not found any available flight
     */
    private void checkEmptyFlightList(List<FlightResponse> flightResponseList) throws NoFlightsFoundException{
        if (flightResponseList.isEmpty()) {
            throw new NoFlightsFoundException();
        }
    }

    /**
     * Check if the direct flight is not earlier that the departure time and not later that the arrival time
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the flight
     * @param arrivalDateTimeAux the arrival time of the flight
     * @return a boolean result that determines if the direct flight meets with the departure and arrival time
     */
    private boolean isValidDirectRouteSchedule(LocalDateTime departureDateTime,
                                               LocalDateTime arrivalDateTime,
                                               LocalDateTime departureDateTimeAux,
                                               LocalDateTime arrivalDateTimeAux) {

        return !departureDateTimeAux.isBefore(departureDateTime) && !arrivalDateTimeAux.isAfter(arrivalDateTime);
    }

    private void checkEmptyRoutes(Optional<Route> directRoute, List<Route> interconnectedRoutes) throws NoRoutesFoundException{
        if (!directRoute.isPresent() && interconnectedRoutes.isEmpty()) {
            throw new NoRoutesFoundException();
        }
    }

    /**
     * Check if the interconnected flights checks the following requirements: <br/>
     * * The interconnected flights are not earlier than the departure time and not later than the arrival time <br/>
     * * The difference between the arrival of the first flight and the departure of the second flight is 2 hours or greater
     *
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeDepartureAirport the departure time of the first flight
     * @param arrivalDateTimeDepartureAirport the arrival time of the first flight
     * @param departureDateTimeArrivalAirport the departure time of the second flight
     * @param arrivalDateTimeArrivalAirport the arrival time of the second flight
     * @return a boolean result that determines if the interconnected flights meet with the requirements above
     */
    private boolean isValidInterconnectedRoutesSchedule(LocalDateTime departureDateTime,
                                                        LocalDateTime arrivalDateTime,
                                                        LocalDateTime departureDateTimeDepartureAirport,
                                                        LocalDateTime arrivalDateTimeDepartureAirport,
                                                        LocalDateTime departureDateTimeArrivalAirport,
                                                        LocalDateTime arrivalDateTimeArrivalAirport) {

        return !departureDateTimeDepartureAirport.isBefore(departureDateTime)
                && !arrivalDateTimeDepartureAirport.isAfter(arrivalDateTime)
                && arrivalDateTimeDepartureAirport.until(departureDateTimeArrivalAirport, ChronoUnit.HOURS) >= 2
                && !arrivalDateTimeArrivalAirport.isAfter(arrivalDateTime);
    }

    /**
     * Make a List of all the direct flights to store in the interconnections response
     * @param route the direct route
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the direct flight
     * @return a List of all the direct flights
     */
    private List<FlightResponse> prepareFlightResponseListDirectRoute(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, LocalDateTime departureDateTimeAux) {
        List<FlightResponse> flightResponseList = new ArrayList<>();

        // First of all, get the only schedule of the direct route
        Schedule directSchedule = getSchedule(route, departureDateTimeAux);

        // Iterate over the schedule to search what direct flights meets with the requirements
        for (Day day : directSchedule.getDays()) {
            departureDateTimeAux = departureDateTimeAux.withDayOfMonth(day.getDay());
            for (Flight flight : day.getFlights()) {
                departureDateTimeAux = departureDateTimeAux.with(flight.getDepartureTime());
                LocalDateTime arrivalDateTimeAux = departureDateTimeAux.with(flight.getArrivalTime());

                // If the direct flight is valid, store it like a FlightResponse
                if (isValidDirectRouteSchedule(
                        departureDateTime,
                        arrivalDateTime,
                        departureDateTimeAux,
                        arrivalDateTimeAux)) {
                    FlightResponse flightResponse = new FlightResponse();
                    flightResponse.setStops(0);
                    List<ScheduleResponse> scheduleResponseList = new ArrayList<>();
                    ScheduleResponse scheduleResponse = new ScheduleResponse();
                    scheduleResponse.setDepartureAirport(route.getAirportFrom());
                    scheduleResponse.setArrivalAirport(route.getAirportTo());
                    scheduleResponse.setDepartureDateTime(departureDateTimeAux);
                    scheduleResponse.setArrivalDateTime(arrivalDateTimeAux);
                    scheduleResponseList.add(scheduleResponse);
                    flightResponse.setLegs(scheduleResponseList);
                    flightResponseList.add(flightResponse);
                }
            }
        }

        return flightResponseList;
    }

    /**
     * Make a List of all the interconnected flights to store in the interconnections response
     *
     * @param routes a list of interconnected routes
     * @param departureAirport the departure airport IATA code
     * @param arrivalAirport the arrival airport IATA code
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the interconnected flights
     * @return a List of all the interconnected flights
     */
    private List<FlightResponse> prepareFlightResponseListInterconnectedRoutes(List<Route> routes, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, LocalDateTime departureDateTimeAux) {
        List<FlightResponse> flightResponseList = new ArrayList<>();

        // Separate the list into a list of first routes and a list of second routes
        List<Route> departureAirportRoutes = routes
                .stream()
                .filter(route -> route.getAirportFrom().equals(departureAirport))
                .collect(Collectors.toList());
        List<Route> arrivalAirportRoutes = routes
                .stream()
                .filter(route -> route.getAirportTo().equals(arrivalAirport))
                .collect(Collectors.toList());

        // Each of the indexes matches with the complete route, so iterate over the same loop
        for (int i = 0; i < departureAirportRoutes.size(); i++) {
            Route departureAirportRoute = departureAirportRoutes.get(i);
            Route arrivalAirportRoute = arrivalAirportRoutes.get(i);

            // Get a schedule of each route
            Schedule departureAirportSchedule = getSchedule(departureAirportRoute, departureDateTimeAux);
            Schedule arrivalAirportSchedule = getSchedule(arrivalAirportRoute, departureDateTimeAux);

            // These nested loops iterates over every combination of flights
            for (Day departureAirportDay : departureAirportSchedule.getDays()) {
                for (Flight departureAirportFlight : departureAirportDay.getFlights()) {
                    for (Day arrivalAirportDay : arrivalAirportSchedule.getDays()) {
                        for (Flight arrivalAirportFlight : arrivalAirportDay.getFlights()) {

                            // Get a time of every flight and store the combination if meets with the requirements
                            LocalDateTime departureDateTimeDepartureAirport = departureDateTimeAux
                                    .withDayOfMonth(departureAirportDay.getDay())
                                    .with(departureAirportFlight.getDepartureTime());
                            LocalDateTime arrivalDateTimeDepartureAirport = departureDateTimeAux
                                    .withDayOfMonth(departureAirportDay.getDay())
                                    .with(departureAirportFlight.getArrivalTime());
                            LocalDateTime departureDateTimeArrivalAirport = departureDateTimeAux
                                    .withDayOfMonth(arrivalAirportDay.getDay())
                                    .with(arrivalAirportFlight.getDepartureTime());
                            LocalDateTime arrivalDateTimeArrivalAirport = departureDateTimeAux
                                    .withDayOfMonth(arrivalAirportDay.getDay())
                                    .with(arrivalAirportFlight.getArrivalTime());

                            if (isValidInterconnectedRoutesSchedule(
                                    departureDateTime,
                                    arrivalDateTime,
                                    departureDateTimeDepartureAirport,
                                    arrivalDateTimeDepartureAirport,
                                    departureDateTimeArrivalAirport,
                                    arrivalDateTimeArrivalAirport)) {

                                FlightResponse flightResponse = new FlightResponse();
                                flightResponse.setStops(1);

                                List<ScheduleResponse> scheduleResponseList = new ArrayList<>();

                                ScheduleResponse scheduleResponseDepartureAirport = new ScheduleResponse();
                                scheduleResponseDepartureAirport.setDepartureAirport(departureAirportRoute.getAirportFrom());
                                scheduleResponseDepartureAirport.setArrivalAirport(departureAirportRoute.getAirportTo());
                                scheduleResponseDepartureAirport.setDepartureDateTime(departureDateTimeDepartureAirport);
                                scheduleResponseDepartureAirport.setArrivalDateTime(arrivalDateTimeDepartureAirport);
                                scheduleResponseList.add(scheduleResponseDepartureAirport);

                                ScheduleResponse scheduleResponseArrivalAirport = new ScheduleResponse();
                                scheduleResponseArrivalAirport.setDepartureAirport(arrivalAirportRoute.getAirportFrom());
                                scheduleResponseArrivalAirport.setArrivalAirport(arrivalAirportRoute.getAirportTo());
                                scheduleResponseArrivalAirport.setDepartureDateTime(departureDateTimeArrivalAirport);
                                scheduleResponseArrivalAirport.setArrivalDateTime(arrivalDateTimeArrivalAirport);
                                scheduleResponseList.add(scheduleResponseArrivalAirport);

                                flightResponse.setLegs(scheduleResponseList);
                                flightResponseList.add(flightResponse);
                            }
                        }
                    }
                }
            }
        }

        return flightResponseList;
    }

    /**
     * Search for flights, either if the routes are direct flights or interconnected flights
     *
     * @param routes a list of routes (direct or interconnected)
     * @param departureAirport the departure airport IATA code
     * @param arrivalAirport the arrival airport IATA code
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @return a list of all the flights searched
     */
    private List<FlightResponse> getFlightsForRoutes(List<Route> routes, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        Period dateInterval = departureDateTime.toLocalDate().until(arrivalDateTime.toLocalDate());
        LocalDateTime departureDateTimeAux = departureDateTime;

        List<FlightResponse> flightResponseList = new ArrayList<>();

        // If there is difference between each time, iterate over every year and month
        for (int i = 0; i <= dateInterval.getYears(); i++) {
            for (int j = 0; j <= dateInterval.getMonths(); j++) {
                if (routes.size() == 1) {
                    flightResponseList = prepareFlightResponseListDirectRoute(routes.get(0), departureDateTime, arrivalDateTime, departureDateTimeAux);
                }
                else {
                    flightResponseList = prepareFlightResponseListInterconnectedRoutes(routes, departureAirport, arrivalAirport, departureDateTime, arrivalDateTime, departureDateTimeAux);
                }
                departureDateTimeAux = departureDateTimeAux.plusMonths(1);
            }
            departureDateTimeAux = departureDateTimeAux.plusYears(1);
        }

        return flightResponseList;
    }

    @Autowired
    public InterconnectionsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.airportMap = new HashMap<>();
    }

    /**
     * Primary method of doing all things to get all available flights
     * @param departure the departure airport IATA code
     * @param arrival the arrival airport IATA code
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @return the final list of flights as a result of the search
     * @throws ResponseStatusException with a client error code (4XX) if there is a problem with the search
     */
    public List<FlightResponse> generateInterconnections(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) throws ResponseStatusException {

        // First of all, check if the departure time is later than the arrival time
        checkDepartureArrivalTime(departureDateTime, arrivalDateTime);

        List<Route> routes = getRoutes();

        // Filter all the routes that meets the requirements
        List<Route> validRoutes = routes
                .stream()
                .filter(route -> filterValidRoutes(route.getConnectingAirport(), route.getOperator()))
                .collect(Collectors.toList());

        // Get a direct route if available
        Optional<Route> directRoute = validRoutes
                .stream()
                .filter(route -> filterDirectRoute(route, departure, arrival))
                .findFirst();

        List<Route> interconnectedRoutes= getInterconnectedRoutes(validRoutes, departure, arrival);

        List<FlightResponse> directRouteFlights = new ArrayList<>();
        List<FlightResponse> interconnectedRoutesFlights = new ArrayList<>();

        // Check if there are no routes
        checkEmptyRoutes(directRoute, interconnectedRoutes);

        // If there is a direct route, get the direct flights
        if (directRoute.isPresent()) {
            List<Route> dr = new ArrayList<>();
            dr.add(directRoute.get());
            directRouteFlights = getFlightsForRoutes(dr, departure, arrival, departureDateTime, arrivalDateTime);
        }

        // Get the interconnected flights if there are interconnected routes
        if (!interconnectedRoutes.isEmpty()) {
            interconnectedRoutesFlights = getFlightsForRoutes(interconnectedRoutes, departure, arrival, departureDateTime, arrivalDateTime);
        }

        // Combine direct flights with interconnected flights
        List<FlightResponse> totalFlightsList = new ArrayList<>(directRouteFlights);
        totalFlightsList.addAll(interconnectedRoutesFlights);

        // Check if there are no flights
        checkEmptyFlightList(totalFlightsList);

        return totalFlightsList;
    }
}