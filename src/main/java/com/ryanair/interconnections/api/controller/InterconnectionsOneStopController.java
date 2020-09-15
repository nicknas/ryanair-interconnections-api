package com.ryanair.interconnections.api.controller;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.service.InterconnectionsOneStopService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handle the /interconnections endpoint of the API
 */
@RestController
@RequestMapping("/interconnections")
@CrossOrigin(origins = "*")
public class InterconnectionsOneStopController implements InterconnectionsController {

    private final InterconnectionsOneStopService interconnectionsOneStopService;

    public InterconnectionsOneStopController(InterconnectionsOneStopService interconnectionsOneStopService) {
        this.interconnectionsOneStopService = interconnectionsOneStopService;
    }

    /**
     * Map the /interconnections GET operation
     * @param departure departure airport IATA code for flight search
     * @param arrival arrival airport IATA code for flight search
     * @param departureDateTime time that starts the flight search
     * @param arrivalDateTime time that finishes the flight search
     * @return a list of flights showing all the schedules of the search
     */
    @GetMapping
    @ResponseBody
    @Override
    public List<FlightResponse> handleInterconnections(@RequestParam String departure,
                                                       @RequestParam String arrival,
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime departureDateTime,
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime arrivalDateTime) {

        return interconnectionsOneStopService.getInterconnections(departure, arrival, departureDateTime, arrivalDateTime);
    }

}
