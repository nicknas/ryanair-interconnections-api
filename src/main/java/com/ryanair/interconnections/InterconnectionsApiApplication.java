package com.ryanair.interconnections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class InterconnectionsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterconnectionsApiApplication.class, args);
    }
}
