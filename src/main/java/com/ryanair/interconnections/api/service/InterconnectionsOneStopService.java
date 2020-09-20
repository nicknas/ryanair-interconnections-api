package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.route.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * A Service class to do all the operations necessary to search the direct and interconnected flights
 */
@Service
public class InterconnectionsOneStopService extends InterconnectionsService{
    private final RoutesOneStopService routesOneStopService;
    private final SchedulesOneStopService schedulesOneStopService;


    @Autowired
    public InterconnectionsOneStopService(RoutesOneStopService routesOneStopService, SchedulesOneStopService schedulesOneStopService) {
        super();
        this.routesOneStopService = routesOneStopService;
        this.schedulesOneStopService = schedulesOneStopService;
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
    @Override
    public List<FlightResponse> getInterconnections(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) throws ResponseStatusException {

        // First of all, check if the departure time is later than the arrival time
        checkDepartureArrivalTime(departureDateTime, arrivalDateTime);

        // Get a direct route if available
        Route directRoute = routesOneStopService.getDirectRoute(departure, arrival);
        List<List<Route>> oneStopRoutes = routesOneStopService.getInterconnectedRoutes(departure, arrival);

        // Check if there are no routes available
        checkEmptyRoutes(directRoute, oneStopRoutes);

        // Get all the flights
        List<FlightResponse> allFlights = schedulesOneStopService.getAllFlights(
                oneStopRoutes,
                directRoute,
                departure,
                arrival,
                departureDateTime,
                arrivalDateTime);

        // Check if there are no flights
        checkEmptyFlightList(allFlights);

        return allFlights;
    }
}