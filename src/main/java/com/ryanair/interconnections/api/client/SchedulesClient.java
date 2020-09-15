package com.ryanair.interconnections.api.client;

import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.api.model.schedule.Schedule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
public class SchedulesClient {

    @Value("${ryanair.api.schedules.url}")
    private String scheduleUrl;

    /**
     * Make a REST API request to Schedules API and stores the result as a Schedule object
     * @param route the route that contains departure and arrival airport
     * @param dateTime time to search the schedules
     * @return a Schedule object representing the result of the Schedules API
     */
    public Schedule getSchedule(Route route, LocalDateTime dateTime) {
        return WebClient
                .create(String.format(
                        scheduleUrl,
                        route.getAirportFrom(),
                        route.getAirportTo(),
                        dateTime.getYear(),
                        dateTime.getMonthValue()))
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Schedule.class)
                .block();
    }
}
