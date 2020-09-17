package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.client.RoutesClient;
import com.ryanair.interconnections.api.model.route.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Service that makes all the route logic in the interconnections service
 */
@Service
public class RoutesOneStopService extends RoutesService{

    @Autowired
    public RoutesOneStopService(RoutesClient routesClient) {
        super(routesClient, new HashMap<>());
    }

    /**
     * Get all the interconnected routes between a departure and an arrival airport
     * @param departure the departure airport IATA code
     * @param arrival the arrival airport IATA code
     * @return a list of all the interconnected routes
     */
    @Override
    public List<List<Route>> getInterconnectedRoutes(String departure, String arrival) {
        // Search all the routes to store each route to the airports map
        routesClient
                .getRoutes()
                .stream()
                .filter(route -> filterValidRoutes(route.getConnectingAirport(), route.getOperator()))
                .forEach(this::addRouteToAirportMap);

        return airportMap.containsKey(departure) ?
                airportMap.get(departure).findInterconnectedRoutes(airportMap, arrival) :
                new ArrayList<>();
    }
}
