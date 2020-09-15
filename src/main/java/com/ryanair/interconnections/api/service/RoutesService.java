package com.ryanair.interconnections.api.service;

import com.ryanair.interconnections.api.model.route.Route;

import java.util.List;

public interface RoutesService {
    List<Route> getInterconnectedRoutes(String departure, String arrival);
}
