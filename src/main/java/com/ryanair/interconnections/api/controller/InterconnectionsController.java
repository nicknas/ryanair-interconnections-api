package com.ryanair.interconnections.api.controller;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface to build an interconnections controller
 */
public interface InterconnectionsController {
    @GetMapping
    @ResponseBody
    List<FlightResponse> handleInterconnections(@RequestParam String departure,
                                                @RequestParam String arrival,
                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime departureDateTime,
                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime arrivalDateTime);
}
