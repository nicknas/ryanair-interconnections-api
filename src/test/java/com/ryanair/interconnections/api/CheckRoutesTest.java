package com.ryanair.interconnections.api;

import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.api.service.RoutesOneStopService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CheckRoutesTest {

    private String[] invalidDepartureAirports;
    private String[] invalidArrivalAirports;
    private String madAirport;
    private String dubAirport;
    private String wroAirport;
    private int interconnectedAirportsMADDUB;
    private int interconnectedAirportsMADWRO;

    private RoutesOneStopService routesService;

    @Autowired
    public CheckRoutesTest(RoutesOneStopService routesService) {
        this.routesService = routesService;
    }

    @BeforeEach
    public void setup() {
        madAirport = "MAD";
        dubAirport = "DUB";
        wroAirport = "WRO";
        interconnectedAirportsMADDUB = 46;
        interconnectedAirportsMADWRO = 16;
        invalidDepartureAirports = new String[] {"ADF", "FDA", "DAEQE"};
        invalidArrivalAirports = new String[] {"FAFA", "FADAD", "FADFA"};
    }

    @Test
    public void checkInvalidRoutes() {
        for (String invalidDepartureAirport : invalidDepartureAirports) {
            for (String invalidArrivalAirport : invalidArrivalAirports) {
                List<List<Route>> oneStopRoutes = routesService.getInterconnectedRoutes(invalidDepartureAirport, invalidArrivalAirport);
                Route directRoute = routesService.getDirectRoute(invalidDepartureAirport, invalidArrivalAirport);

                Assertions.assertNull(directRoute);
                Assertions.assertEquals(0, oneStopRoutes.size());
            }
        }
    }

    @Test
    public void checkRoutesMADDUB() {
        List<List<Route>> oneStopRoutes = routesService.getInterconnectedRoutes(madAirport, dubAirport);
        Route directRoute = routesService.getDirectRoute(madAirport, dubAirport);

        Assertions.assertNotNull(directRoute);
        Assertions.assertEquals(interconnectedAirportsMADDUB, oneStopRoutes.size());
    }

    @Test
    public void checkRoutesWROMAD() {
        List<List<Route>> oneStopRoutes = routesService.getInterconnectedRoutes(wroAirport, madAirport);
        Route directRoute = routesService.getDirectRoute(wroAirport, madAirport);

        Assertions.assertNotNull(directRoute);
        Assertions.assertEquals(interconnectedAirportsMADWRO, oneStopRoutes.size());
    }
}
