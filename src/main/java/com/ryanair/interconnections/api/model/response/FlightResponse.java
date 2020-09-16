package com.ryanair.interconnections.api.model.response;

import java.util.List;

/**
 * POJO that represents each flight of the interconnections response
 */
public class FlightResponse {
    private int stops;
    private List<FlightLegResponse> legs;

    public int getStops() {
        return stops;
    }

    public void setStops(int stops) {
        this.stops = stops;
    }

    public List<FlightLegResponse> getLegs() {
        return legs;
    }

    public void setLegs(List<FlightLegResponse> legs) {
        this.legs = legs;
    }
}
