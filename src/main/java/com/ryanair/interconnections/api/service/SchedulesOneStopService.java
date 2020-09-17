package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.client.SchedulesClient;
import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.response.FlightLegResponse;
import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.api.model.schedule.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that makes all the schedule logic in the interconnections service
 */
@Service
public class SchedulesOneStopService extends SchedulesService{

    @Autowired
    public SchedulesOneStopService(SchedulesClient schedulesClient) {
        super(schedulesClient);
    }

    /**
     * Check if the one stop flights checks the following requirements: <br/>
     * * The one stop flights are not earlier than the departure time and not later than the arrival time <br/>
     * * The difference between the arrival of the first leg flight and the departure of the second leg flight is 2 hours or greater
     *
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param firstLegDepartureDateTime the departure time of the first leg flight
     * @param firstLegArrivalDateTime the arrival time of the first leg flight
     * @param secondLegDepartureDateTime the departure time of the second leg flight
     * @param secondLegArrivalDateTime the arrival time of the second leg flight
     * @return a boolean result that determines if the one stop flights meet with the requirements above
     */
    private boolean isValidOneStopFlight(LocalDateTime departureDateTime,
                                                        LocalDateTime arrivalDateTime,
                                                        LocalDateTime firstLegDepartureDateTime,
                                                        LocalDateTime firstLegArrivalDateTime,
                                                        LocalDateTime secondLegDepartureDateTime,
                                                        LocalDateTime secondLegArrivalDateTime) {

        return !firstLegDepartureDateTime.isBefore(departureDateTime)
                && !firstLegArrivalDateTime.isAfter(arrivalDateTime)
                && firstLegArrivalDateTime.until(secondLegDepartureDateTime, ChronoUnit.HOURS) >= 2
                && !secondLegArrivalDateTime.isAfter(arrivalDateTime);
    }

    /**
     * Make a List of all the one stop flights to store in the interconnections response
     *
     * @param oneStopRoutes a list of one stop routes
     * @param departureDateTime the departure time limit
     * @param arrivalDateTime the arrival time limit
     * @param departureDateTimeAux the departure time of the one stop flights
     * @return a List of all the one stop flights
     */
    private List<FlightResponse> getOneStopFlights(List<List<Route>> oneStopRoutes, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, LocalDateTime departureDateTimeAux) {
        return oneStopRoutes.parallelStream()
                .flatMap(oneStopRoute -> {
                    Route firstLegRoute = oneStopRoute.get(0);
                    Route secondLegRoute = oneStopRoute.get(1);

                    // Get a schedule of each route
                    Schedule firstLegSchedule = schedulesClient.getSchedule(firstLegRoute, departureDateTimeAux);
                    Schedule secondLegSchedule = schedulesClient.getSchedule(secondLegRoute, departureDateTimeAux);

                    return firstLegSchedule.getDays()
                            .stream()
                            .flatMap(firstLegDay -> firstLegDay.getFlights()
                            .stream()
                            .flatMap(firstLegFlight -> secondLegSchedule.getDays()
                            .stream()
                            .flatMap(secondLegDay -> secondLegDay.getFlights()
                            .stream()
                            .map(secondLegFlight -> new FlightResponse(1, Arrays.asList(
                                    new FlightLegResponse(firstLegRoute.getAirportFrom(),
                                            firstLegRoute.getAirportTo(),
                                            departureDateTimeAux.withDayOfMonth(firstLegDay.getDay()).with(firstLegFlight.getDepartureTime()),
                                            departureDateTimeAux.withDayOfMonth(firstLegDay.getDay()).with(firstLegFlight.getArrivalTime())),
                                    new FlightLegResponse(secondLegRoute.getAirportFrom(),
                                            secondLegRoute.getAirportTo(),
                                            departureDateTimeAux.withDayOfMonth(secondLegDay.getDay()).with(secondLegFlight.getDepartureTime()),
                                            departureDateTimeAux.withDayOfMonth(secondLegDay.getDay()).with(secondLegFlight.getArrivalTime())))))
                            )));

                })
                .filter(flightResponse -> isValidOneStopFlight(
                        departureDateTime,
                        arrivalDateTime,
                        flightResponse.getLegs().get(0).getDepartureDateTime(),
                        flightResponse.getLegs().get(0).getArrivalDateTime(),
                        flightResponse.getLegs().get(1).getDepartureDateTime(),
                        flightResponse.getLegs().get(1).getArrivalDateTime()))
                .collect(Collectors.toList());
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
    public List<FlightResponse> getFlightsForRoutes(List<List<Route>> oneStopRoutes, Route directRoute, String departureAirport, String arrivalAirport, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
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
                oneStopFlightResponseList.addAll(getOneStopFlights(oneStopRoutes, departureDateTime, arrivalDateTime, departureDateTimeAux));

                departureDateTimeAux = departureDateTimeAux.plusMonths(1);
            }
            departureDateTimeAux = departureDateTimeAux.plusYears(1);
        }

        allFlightResponseList.addAll(directFlightResponseList);
        allFlightResponseList.addAll(oneStopFlightResponseList);

        return allFlightResponseList;
    }
}
