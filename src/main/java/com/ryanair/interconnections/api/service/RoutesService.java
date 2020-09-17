package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.client.RoutesClient;
import com.ryanair.interconnections.api.model.airport.Airport;
import com.ryanair.interconnections.api.model.route.Route;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

/**
 * Interface to build a routes service
 */
public abstract class RoutesService {
    protected final RoutesClient routesClient;
    protected final Map<String, Airport> airportMap;

    @Value("${ryanair.literal}")
    protected String ryanairLiteral;

    /**
     * Filter if a route has connectingAirport to null and the operator of the route is Ryanair
     * @param connectingAirport the connecting airport of a route
     * @param operator the operator of a route
     * @return a boolean result determining if a route is valid
     */
    protected boolean filterValidRoutes(String connectingAirport, String operator) {
        return connectingAirport == null && operator.equals(ryanairLiteral);
    }

    /**
     * Filter if a route is a direct flight
     * @param route the route to check if is a direct flight
     * @param departure the departure airport IATA code to check
     * @param arrival the arrival airport IATA code to check
     * @return a boolean result determining if a route comes from a direct flight
     */
    protected boolean filterDirectRoute(Route route, String departure, String arrival) {
        return route.getAirportFrom().equals(departure) && route.getAirportTo().equals(arrival);
    }

    /**
     * Add a route in an airport of the map. If the airport does not exist, it creates a new Airport with the route
     * @param route the route to add
     */
    protected void addRouteToAirportMap(Route route) {
        // if the airport exists in the map, just add a new route
        if (airportMap.containsKey(route.getAirportFrom())) {
            airportMap.get(route.getAirportFrom()).addRoute(route);
        }
        // create a new airport with the first route
        else {
            airportMap.put(route.getAirportFrom(), new Airport(route));
        }
    }

    public RoutesService(RoutesClient routesClient, Map<String, Airport> airportMap) {
        this.routesClient = routesClient;
        this.airportMap = airportMap;
    }

    /**
     * From a departure airport and an arrival airport, get a direct route between them
     * @param departure the departure airport
     * @param arrival the arrival airport
     * @return the direct route between the airports, or null if the route does not exist
     */
    public Route getDirectRoute(String departure, String arrival) {
        return routesClient
                .getRoutes()
                .stream()
                .filter(route -> filterValidRoutes(route.getConnectingAirport(), route.getOperator()))
                .filter(route -> filterDirectRoute(route, departure, arrival))
                .findFirst()
                .orElse(null);
    }

    abstract List<List<Route>> getInterconnectedRoutes(String departure, String arrival);
}
