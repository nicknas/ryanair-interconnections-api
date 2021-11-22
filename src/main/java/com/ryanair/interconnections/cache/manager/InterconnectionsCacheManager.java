package com.ryanair.interconnections.cache.manager;

import java.time.LocalDateTime;

import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.response.InterconnectionResponse;
import com.ryanair.interconnections.api.model.route.Route;
import com.ryanair.interconnections.cache.config.InterconnectionsCacheConfig;
import com.ryanair.interconnections.cache.schema.InterconnectionsSchemaBuilderImpl;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InterconnectionsCacheManager {
    
    private RemoteCacheManager manager;

    @Autowired
    public InterconnectionsCacheManager(RemoteCacheManager manager) {
        this.manager = manager;
        RemoteCache<String, String> metadataCache = manager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
        GeneratedSchema flightSchema = new InterconnectionsSchemaBuilderImpl();
        metadataCache.put(flightSchema.getProtoFileName(), flightSchema.getProtoFile());
    }

    @Bean
    RemoteCache<String, InterconnectionResponse> flightProtoCache() {
        return manager.getCache(InterconnectionsCacheConfig.FLIGHT_CACHE);
    }

    @Bean
    RemoteCache<String, FlightResponse> singleFlightProtoCache() {
        return manager.getCache(InterconnectionsCacheConfig.FLIGHT_CACHE);
    }

    @Bean
    RemoteCache<String, LocalDateTime> dateProtoCache() {
        return manager.getCache(InterconnectionsCacheConfig.FLIGHT_CACHE);
    }

    @Bean
    RemoteCache<String, Route> routeProtoCache() {
        return manager.getCache(InterconnectionsCacheConfig.FLIGHT_CACHE);
    }
}
