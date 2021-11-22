package com.ryanair.interconnections.api.model.response;

import java.util.ArrayList;
import java.util.List;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class InterconnectionResponse {
    private List<FlightResponse> flightList;

    @ProtoFactory
    public InterconnectionResponse(List<FlightResponse> flightList) {
        this.flightList = flightList;
    }

    @ProtoField(number = 1, collectionImplementation = ArrayList.class)
    public List<FlightResponse> getFlightList() {
        return flightList;
    }

    public void setFlightList(List<FlightResponse> flightList) {
        this.flightList = new ArrayList<>(flightList);
    }
}
