package com.ryanair.interconnections.api;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.service.InterconnectionsOneStopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class CheckNumberOfFlightsTest {
    private String[] departureAirports;
    private String[] arrivalAirports;
    private LocalDateTime[] departureTime;
    private LocalDateTime[] arrivalTime;
    private int[] expectedNumberOfFlights;

    private final InterconnectionsOneStopService interconnectionsOneStopService;

    @Autowired
    public CheckNumberOfFlightsTest(InterconnectionsOneStopService interconnectionsOneStopService) {
        this.interconnectionsOneStopService = interconnectionsOneStopService;
    }


    @BeforeEach
    public void setup() {
        departureAirports = new String[]{
                "MAD",
                "DUB",
                "MAD",
                "MAD"};
        arrivalAirports = new String[]{
                "WRO",
                "WRO",
                "DUB",
                "DUB"};
        departureTime = new LocalDateTime[]{
                LocalDateTime.parse("2020-09-17T06:00"),
                LocalDateTime.parse("2020-09-17T06:00"),
                LocalDateTime.parse("2020-09-17T06:00"),
                LocalDateTime.parse("2020-09-17T06:00")
        };
        arrivalTime = new LocalDateTime[]{
                LocalDateTime.parse("2020-09-17T21:00"),
                LocalDateTime.parse("2020-09-17T18:00"),
                LocalDateTime.parse("2020-09-17T18:00"),
                LocalDateTime.parse("2020-10-17T18:00")};
        expectedNumberOfFlights = new int[]{1, 2, 15, 17975};
    }

    @Test
    public void checkFlights() {
        for (int i = 0; i < departureAirports.length; i++) {
            List<FlightResponse> flightResponseList = interconnectionsOneStopService.getInterconnections(departureAirports[i], arrivalAirports[i], departureTime[i], arrivalTime[i]);
            Assertions.assertEquals(expectedNumberOfFlights[i], flightResponseList.size());
        }
    }
}
