package com.ryanair.interconnections.api.model.response;

import java.util.ArrayList;
import java.util.List;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

/**
 * POJO that represents each flight of the interconnections response
 */
public class FlightResponse {

    private int stops;
    private List<FlightLegResponse> legs;

    @ProtoFactory
    public FlightResponse(int stops, List<FlightLegResponse> legs) {
        this.stops = stops;
        this.legs = legs;
    }

    @ProtoField(number = 1, required = true)
    public int getStops() {
        return stops;
    }

    public void setStops(int stops) {
        this.stops = stops;
    }

    @ProtoField(number = 2, collectionImplementation = ArrayList.class)
    public List<FlightLegResponse> getLegs() {
        return legs;
    }

    public void setLegs(List<FlightLegResponse> legs) {
        this.legs = legs;
    }
}
