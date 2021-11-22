package com.ryanair.interconnections.cache.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.response.InterconnectionResponse;
import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.cache.config.InterconnectionsCacheConfig;

import org.infinispan.client.hotrod.RemoteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Repository;

@Repository
@CacheConfig(cacheNames = {InterconnectionsCacheConfig.FLIGHT_CACHE})
public class FlightRepository {
    
    @Autowired
    @Qualifier("flightProtoCache")
    private RemoteCache<String, InterconnectionResponse> flightCache;


    @Autowired
    @Qualifier("singleFlightProtoCache")
    private RemoteCache<String, FlightResponse> singleFlightCache;

    @Autowired
    @Qualifier("dateProtoCache")
    private RemoteCache<String, LocalDateTime> dateCache;

    @Autowired
    @Qualifier("routeProtoCache")
    private RemoteCache<String, Route> routeCache;

    public InterconnectionResponse findById(String id){
        return flightCache.get(id);
    }

    public FlightResponse findSingleFlightById(String id){
        return singleFlightCache.get(id);
    }

    public Route findRouteById(String id){
        return routeCache.get(id);
    }

    public CompletableFuture<InterconnectionResponse> findByIdAsync(String id) {
        return flightCache.getAsync(id);
    }

    public void insert(String id, InterconnectionResponse flight){
        flightCache.put(id, flight);
    }

    public void insertSingleFlight(String id, FlightResponse sFlight){
        singleFlightCache.put(id, sFlight);
    }

    public void insertDateTime(String id, LocalDateTime date){
        dateCache.put(id, date);
    }

    public void insertRoute(String id, Route route){
        routeCache.put(id, route);
    }

    public void insertAsync(String id, InterconnectionResponse flight){
        flightCache.putAsync(id, flight);
    }

    public void delete(String id){
        flightCache.remove(id);
    }

    public boolean bulkRemove(Set<String> keys){
        return flightCache.keySet().removeAll(keys);
    }

    public void deleteAll(){
        flightCache.clear();
    }

    public int getSize(){
        return flightCache.size();
    }

    public String getKeys(){
        return flightCache.keySet().toString();
    }

    public String getValues(){
        return flightCache.values().toString();
    }

    public List<InterconnectionResponse> findByDeparture(String departure) {
        return flightCache.values().parallelStream()
        .filter(i -> i
        .getFlightList().parallelStream()
        .filter(f -> f
        .getLegs().parallelStream()
        .anyMatch(l -> l.getDepartureAirport().equals(departure)))
        .count() > 0)
        .collect(Collectors.toList());
    }
}
