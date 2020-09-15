package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.client.RoutesClient;
import com.ryanair.interconnections.api.model.airport.Airport;
import com.ryanair.interconnections.api.model.route.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoutesOneStopService implements RoutesService{

    private final RoutesClient routesClient;
    private final Map<String, Airport> airportMap;

    @Value("${ryanair.literal}")
    private String ryanairLiteral;

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

    private void addRouteToAirportMap(Route route) {
        // if the airport exists in the map, just add a new route
        if (airportMap.containsKey(route.getAirportFrom())) {
            airportMap.get(route.getAirportFrom()).addRoute(route);
        }
        // create a new airport with the first route
        else {
            airportMap.put(route.getAirportFrom(), new Airport(route));
        }
    }

    @Autowired
    public RoutesOneStopService(RoutesClient routesClient) {
        this.routesClient = routesClient;
        this.airportMap = new HashMap<>();
    }

    public Route getDirectRoute(String departure, String arrival) {
        return routesClient.getRoutes()
                .stream()
                .filter(route -> filterValidRoutes(route.getConnectingAirport(), route.getOperator()))
                .filter(route -> filterDirectRoute(route, departure, arrival))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all the interconnected routes between a departure and an arrival airport
     * @param departure the departure airport IATA code
     * @param arrival the arrival airport IATA code
     * @return a list of all the interconnected routes
     */
    @Override
    public List<Route> getInterconnectedRoutes(String departure, String arrival) {
        // Search all the routes to store each route to the airports map
        routesClient.getRoutes()
                .stream()
                .filter(route -> filterValidRoutes(route.getConnectingAirport(), route.getOperator()))
                .forEach(validRoute -> addRouteToAirportMap(validRoute));

        return airportMap.get(departure).findInterconnectedRoutes(airportMap, arrival);
    }
}
