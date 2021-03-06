package com.ryanair.interconnections.api;

import com.ryanair.interconnections.api.exception.DepartureAfterArrivalException;
import com.ryanair.interconnections.api.exception.NoFlightsFoundException;
import com.ryanair.interconnections.api.exception.NoRoutesFoundException;
import com.ryanair.interconnections.api.service.InterconnectionsOneStopService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class CheckInvalidFlightsTest {

    private LocalDateTime[] departureTimes;
    private LocalDateTime[] arrivalTimes;
    private String[] departureAirports;
    private String[] arrivalAirports;

    private String[] invalidDepartureAirports;
    private String[] invalidArrivalAirports;
    private LocalDateTime[] invalidDepartureTimes;
    private LocalDateTime[] invalidArrivalTimes;

    private String[] noFlightsDepartureAirports;
    private String[] noFlightsArrivalAirports;
    private LocalDateTime[] noFlightsDepartureTimes;
    private LocalDateTime[] noFlightsArrivalTimes;

    private final InterconnectionsOneStopService interconnectionsOneStopService;

    @Autowired
    public CheckInvalidFlightsTest(InterconnectionsOneStopService interconnectionsOneStopService) {
        this.interconnectionsOneStopService = interconnectionsOneStopService;
    }


    @BeforeEach
    public void setup() {
        departureAirports = new String[]{"MAD", "WRO", "DUB"};
        arrivalAirports = new String[]{"DUB", "MAD", "WRO"};
        departureTimes = new LocalDateTime[]{
                LocalDateTime.parse("2020-09-17T06:00"),
                LocalDateTime.parse("2020-09-18T08:00"),
                LocalDateTime.parse("2020-09-18T10:00")
        };
        arrivalTimes = new LocalDateTime[]{
                LocalDateTime.parse("2020-09-17T22:00"),
                LocalDateTime.parse("2020-09-18T21:00"),
                LocalDateTime.parse("2020-10-19T20:00")
        };
        invalidDepartureAirports = new String[]{"FADFE", "FASF", "FDSf"};
        invalidArrivalAirports = new String[]{"dfasdf", "FDAfds", "<Df"};
        invalidDepartureTimes = departureTimes;
        invalidArrivalTimes = new LocalDateTime[] {
                LocalDateTime.parse("2020-09-16T06:00"),
                LocalDateTime.parse("2020-09-17T08:00"),
                LocalDateTime.parse("2020-09-17T10:00")
        };
        noFlightsDepartureAirports = departureAirports;
        noFlightsArrivalAirports = arrivalAirports;
        noFlightsDepartureTimes = new LocalDateTime[]{
                LocalDateTime.parse("2012-09-16T06:00"),
                LocalDateTime.parse("2011-09-17T08:00"),
                LocalDateTime.parse("2014-09-17T10:00")
        };
        noFlightsArrivalTimes = new LocalDateTime[] {
                LocalDateTime.parse("2012-09-17T06:00"),
                LocalDateTime.parse("2011-09-18T08:00"),
                LocalDateTime.parse("2014-10-18T10:00")
        };
    }

    @Test
    public void checkNoRoutesFoundException() {
        for (int i = 0; i < invalidDepartureAirports.length; i++) {
            int index = i;
            Assertions.assertThrows(NoRoutesFoundException.class, () -> interconnectionsOneStopService.getInterconnections(
                    invalidDepartureAirports[index],
                    invalidArrivalAirports[index],
                    departureTimes[index],
                    arrivalTimes[index]));
        }
    }

    @Test
    public void checkDepartureAfterArrivalException() {
        for (int i = 0; i < departureAirports.length; i++) {
            int index = i;
            Assertions.assertThrows(DepartureAfterArrivalException.class, () -> interconnectionsOneStopService.getInterconnections(
                    departureAirports[index],
                    arrivalAirports[index],
                    invalidDepartureTimes[index],
                    invalidArrivalTimes[index]));
        }
    }

    @Test
    public void checkNoFlightsFoundException() {
        for (int i = 0; i < noFlightsDepartureAirports.length; i++) {
            int index = i;
            Assertions.assertThrows(NoFlightsFoundException.class, () -> interconnectionsOneStopService.getInterconnections(
                    noFlightsDepartureAirports[index],
                    noFlightsArrivalAirports[index],
                    noFlightsDepartureTimes[index],
                    noFlightsArrivalTimes[index]));
        }
    }
}
