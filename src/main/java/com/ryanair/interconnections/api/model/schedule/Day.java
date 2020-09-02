package com.ryanair.interconnections.api.model.schedule;

import java.util.List;

/**
 * POJO that represents each day of the Schedules API response
 */
public class Day {
    private int day;
    private List<Flight> flights;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }
}
