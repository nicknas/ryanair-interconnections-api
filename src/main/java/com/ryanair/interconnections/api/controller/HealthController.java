package com.ryanair.interconnections.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*")
public class HealthController {
    
    @GetMapping
    public HttpStatus health() {return HttpStatus.OK;}
}
