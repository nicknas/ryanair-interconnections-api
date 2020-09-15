package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.client.SchedulesClient;
import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.response.ScheduleResponse;
import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.api.model.schedule.Day;
import com.ryanair.interconnections.api.model.schedule.Flight;
import com.ryanair.interconnections.api.model.schedule.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that makes all the schedule logic in the interconnections service
 */
@Service
public class SchedulesOneStopService implements SchedulesService{

    private final SchedulesClient schedulesClient;

    @Autowired
    public SchedulesOneStopService(SchedulesClient schedulesClient) {
        this.schedulesClient = schedulesClient;
    }

    /**
     * Check if the one stop flights checks the following requirements: <br/>
     * * The one stop flights are not earlier than the departure time and not later than the arrival time <br/>
     * * The difference between the arrival of the first leg flight and the departure of the second leg flight is 2 hours or greater
     *
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeDepartureAirport the departure time of the first leg flight
     * @param arrivalDateTimeDepartureAirport the arrival time of the first leg flight
     * @param departureDateTimeArrivalAirport the departure time of the second leg flight
     * @param arrivalDateTimeArrivalAirport the arrival time of the second leg flight
     * @return a boolean result that determines if the one stop flights meet with the requirements above
     */
    private boolean isValidOneStopSchedule(LocalDateTime departureDateTime,
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

    /**
     * Make a List of all the direct flights to store in the interconnections response
     * @param route the direct route
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the direct flight
     * @return a List of all the direct flights
     */
    private List<FlightResponse> getDirectRouteFlights(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, LocalDateTime departureDateTimeAux) {
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
    private void addDirectFlightToList(List<FlightResponse> flightResponseList,
                                 Route route,
                                 LocalDateTime departureDateTime,
                                 LocalDateTime arrivalDateTime,
                                 LocalDateTime departureDateTimeAux,
                                 Day day,
                                 Flight flight) {
        ScheduleResponse directFlightResponse = new ScheduleResponse(route.getAirportFrom(),
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
            List<ScheduleResponse> scheduleResponseList = new ArrayList<>();
            scheduleResponseList.add(directFlightResponse);
            flightResponse.setLegs(scheduleResponseList);
            flightResponseList.add(flightResponse);
        }
    }

    /**
     * Make a List of all the one stop flights to store in the interconnections response
     *
     * @param routes a list of interconnected routes
     * @param departureAirport the departure airport IATA code
     * @param arrivalAirport the arrival airport IATA code
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the one stop flights
     * @return a List of all the one stop flights
     */
    private List<FlightResponse> getOneStopFlights(List<Route> routes, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, LocalDateTime departureDateTimeAux) {
        List<FlightResponse> flightResponseList = new ArrayList<>();

        // Separate the list into a list of first leg routes and a list of second leg routes
        List<Route> firstLegRoutes = routes
                .stream()
                .filter(route -> route.getAirportFrom().equals(departureAirport))
                .collect(Collectors.toList());
        List<Route> secondLegRoutes = routes
                .stream()
                .filter(route -> route.getAirportTo().equals(arrivalAirport))
                .collect(Collectors.toList());

        // Each of the indexes matches with the complete route, so iterate over the same loop
        for (int i = 0; i < firstLegRoutes.size(); i++) {
            Route firstLegRoute = firstLegRoutes.get(i);
            Route secondLegRoute = secondLegRoutes.get(i);

            // Get a schedule of each route
            Schedule firstLegSchedule = schedulesClient.getSchedule(firstLegRoute, departureDateTimeAux);
            Schedule secondLegSchedule = schedulesClient.getSchedule(secondLegRoute, departureDateTimeAux);

            firstLegSchedule
                    .getDays()
                    .forEach(firstLegDay -> firstLegDay.getFlights()
                    .forEach(firstLegFlight -> secondLegSchedule.getDays()
                    .forEach(secondLegDay -> secondLegDay.getFlights()
                    .forEach(secondLegFlight -> addOneStopFlightToList(
                            flightResponseList,
                            firstLegRoute,
                            secondLegRoute,
                            departureDateTime,
                            arrivalDateTime,
                            departureDateTimeAux,
                            firstLegDay,
                            secondLegDay,
                            firstLegFlight,
                            secondLegFlight)))));
        }

        return flightResponseList;
    }

    /**
     * Add a one stop flight to the flight response list, if meets with the requirements
     * @param flightResponseList the list of flight response
     * @param firstLegRoute the route of the first leg
     * @param secondLegRoute the route of the second leg
     * @param departureDateTime the departure time
     * @param arrivalDateTime the arrival time
     * @param departureDateTimeAux the departure time of the flight to add
     * @param firstLegDay a day object that represents the day of the first leg flight
     * @param secondLegDay a day object that represents the day of the second leg flight
     * @param firsLegFlight a flight object that represents the first leg flight to add
     * @param secondLegFlight a flight object that represents the second leg flight to add
     */
    private void addOneStopFlightToList(List<FlightResponse> flightResponseList,
                                        Route firstLegRoute,
                                        Route secondLegRoute,
                                        LocalDateTime departureDateTime,
                                        LocalDateTime arrivalDateTime,
                                        LocalDateTime departureDateTimeAux,
                                        Day firstLegDay,
                                        Day secondLegDay,
                                        Flight firsLegFlight,
                                        Flight secondLegFlight) {


        ScheduleResponse firstLegFlightResponse = new ScheduleResponse(firstLegRoute.getAirportFrom(),
                firstLegRoute.getAirportTo(),
                departureDateTimeAux.withDayOfMonth(firstLegDay.getDay()).with(firsLegFlight.getDepartureTime()),
                departureDateTimeAux.withDayOfMonth(firstLegDay.getDay()).with(firsLegFlight.getArrivalTime()));

        ScheduleResponse secondLegFlightResponse = new ScheduleResponse(secondLegRoute.getAirportFrom(),
                secondLegRoute.getAirportTo(),
                departureDateTimeAux.withDayOfMonth(secondLegDay.getDay()).with(secondLegFlight.getDepartureTime()),
                departureDateTimeAux.withDayOfMonth(secondLegDay.getDay()).with(secondLegFlight.getArrivalTime()));

        if (isValidOneStopSchedule(
                departureDateTime,
                arrivalDateTime,
                firstLegFlightResponse.getDepartureDateTime(),
                firstLegFlightResponse.getArrivalDateTime(),
                secondLegFlightResponse.getDepartureDateTime(),
                secondLegFlightResponse.getArrivalDateTime())) {

            FlightResponse flightResponse = new FlightResponse();
            flightResponse.setStops(1);

            List<ScheduleResponse> allLegsList = new ArrayList<>();

            allLegsList.add(firstLegFlightResponse);
            allLegsList.add(secondLegFlightResponse);

            flightResponse.setLegs(allLegsList);
            flightResponseList.add(flightResponse);
        }

    }

    /**
     * Search for all the flights
     *
     * @param oneStopRoutes a list of one stop routes
     * @param directRoute the only direct route
     * @param departureAirport the departure airport IATA code
     * @param arrivalAirport the arrival airport IATA code
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @return a list of all the flights searched
     */
    @Override
    public List<FlightResponse> getFlightsForRoutes(List<Route> oneStopRoutes, Route directRoute, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        Period dateInterval = departureDateTime.toLocalDate().until(arrivalDateTime.toLocalDate());
        LocalDateTime departureDateTimeAux = departureDateTime;

        List<FlightResponse> directFlightResponseList = new ArrayList<>();
        List<FlightResponse> oneStopFlightResponseList = new ArrayList<>();
        List<FlightResponse> allFlightResponseList = new ArrayList<>();

        // If there is difference of years or months between each time, iterate over every year and month
        for (int i = 0; i <= dateInterval.getYears(); i++) {
            for (int j = 0; j <= dateInterval.getMonths(); j++) {
                if (directRoute != null) {
                    directFlightResponseList.addAll(getDirectRouteFlights(directRoute, departureDateTime, arrivalDateTime, departureDateTimeAux));
                }
                oneStopFlightResponseList.addAll(getOneStopFlights(oneStopRoutes, departureAirport, arrivalAirport, departureDateTime, arrivalDateTime, departureDateTimeAux));

                departureDateTimeAux = departureDateTimeAux.plusMonths(1);
            }
            departureDateTimeAux = departureDateTimeAux.plusYears(1);
        }

        allFlightResponseList.addAll(directFlightResponseList);
        allFlightResponseList.addAll(oneStopFlightResponseList);

        return allFlightResponseList;
    }
}
