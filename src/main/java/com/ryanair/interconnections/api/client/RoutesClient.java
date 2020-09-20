package com.ryanair.interconnections.api.client;

import com.ryanair.interconnections.api.model.route.Route;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Class that represents the Routes API, using WebClient to communicate with the API
 */
@Service
public class RoutesClient {

    @Value("${ryanair.api.routes.url}")
    private String routeUrl;

    /**
     * Make a REST API request to Routes API and stores the result as a list of routes
     * @return the list of routes
     */
    public List<Route> getRoutes() {
        return WebClient
                .create(routeUrl)
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Route.class)
                .collectList()
                .block();
    }
}
