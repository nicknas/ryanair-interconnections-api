package com.ryanair.interconnections.api.model.response;

import java.util.List;

/**
 * POJO that represents each flight of the interconnections response
 */
public class FlightResponse {
    private int stops;
    private List<ScheduleResponse> legs;

    public int getStops() {
        return stops;
    }

    public void setStops(int stops) {
        this.stops = stops;
    }

    public List<ScheduleResponse> getLegs() {
        return legs;
    }

    public void setLegs(List<ScheduleResponse> legs) {
        this.legs = legs;
    }
}
