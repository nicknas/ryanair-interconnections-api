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
        for (int i = 0; i < invalidDepartureAirports.length; i++) {
            for (int j = 0; j < invalidArrivalAirports.length; j++) {
                List<Route> oneStopRoutes = routesService.getInterconnectedRoutes(invalidDepartureAirports[i], invalidArrivalAirports[j]);
                Route directRoute = routesService.getDirectRoute(invalidDepartureAirports[i], invalidArrivalAirports[j]);

                Assertions.assertNull(directRoute);
                Assertions.assertEquals(0, oneStopRoutes.size());
            }
        }
    }

    @Test
    public void checkRoutesMADDUB() {
        List<Route> oneStopRoutes = routesService.getInterconnectedRoutes(madAirport, dubAirport);
        Route directRoute = routesService.getDirectRoute(madAirport, dubAirport);

        Assertions.assertNotNull(directRoute);
        Assertions.assertEquals(interconnectedAirportsMADDUB * 2, oneStopRoutes.size());
    }

    @Test
    public void checkRoutesWROMAD() {
        List<Route> oneStopRoutes = routesService.getInterconnectedRoutes(wroAirport, madAirport);
        Route directRoute = routesService.getDirectRoute(wroAirport, madAirport);

        Assertions.assertNotNull(directRoute);
        Assertions.assertEquals(interconnectedAirportsMADWRO * 2, oneStopRoutes.size());
    }
}
