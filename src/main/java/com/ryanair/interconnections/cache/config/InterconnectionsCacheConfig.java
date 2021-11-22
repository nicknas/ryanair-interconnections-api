package com.ryanair.interconnections.cache.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.Ordered;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;

import java.io.IOException;
import com.ryanair.interconnections.cache.schema.InterconnectionsSchemaBuilderImpl;

import org.infinispan.commons.marshall.ProtoStreamMarshaller;


@Configuration
public class InterconnectionsCacheConfig {

   public static final String FLIGHT_CACHE = "flight";

   @Value("classpath:cache/flight.xml")
   private Resource flightResource;
   
   @Bean
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public InfinispanRemoteCacheCustomizer caches() {
      return b -> {
         try {
            b
            .marshaller(new ProtoStreamMarshaller())
            .addContextInitializer(new InterconnectionsSchemaBuilderImpl());

            b.remoteCache(FLIGHT_CACHE)
            .configurationURI(flightResource.getURI())
            .marshaller(ProtoStreamMarshaller.class);
                        
         } catch (IOException e) {
               e.printStackTrace();
         }
      };
   }
}
