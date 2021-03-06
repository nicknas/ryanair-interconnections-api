package com.ryanair.interconnections.api.model.airport;

import com.ryanair.interconnections.api.model.route.Route;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Represent an airport with all the possible routes, using the destination as a key of the route map
 */
public class Airport {

    private final Map<String, Route> routeMap;

    public Airport(Route route) {
        this.routeMap = new HashMap<>();
        this.routeMap.put(route.getAirportTo(), route);
    }

    /**
     * Add a route to the map
     * @param route the route to add
     */
    public void addRoute(Route route) {
        routeMap.put(route.getAirportTo(), route);
    }

    /**
     * Search for interconnected routes and store it in the airport
     * @param airportMap the map of all airports found in the Routes API
     * @param arrival the arrival airport IATA code
     * @return a List of all the interconnected routes
     */
    public List<List<Route>> findInterconnectedRoutes(Map<String, Airport> airportMap, String arrival) {
        return routeMap.entrySet()
                .stream()
                .filter(entry -> airportMap.get(entry.getKey()).routeMap.containsKey(arrival))
                .map(entry -> Arrays.asList(entry.getValue(), airportMap.get(entry.getKey()).routeMap.get(arrival)))
                .collect(Collectors.toList());
    }
}
