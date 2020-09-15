package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.model.route.Route;

import java.util.List;

/**
 * Interface to build a routes service
 */
public interface RoutesService {
    List<Route> getInterconnectedRoutes(String departure, String arrival);
}
