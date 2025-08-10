package com.stofina.app.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping(value = "/user-service", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> userServiceFallback() {
        return buildResponse("User Service is currently unavailable");
    }

    @GetMapping(value = "/portfolio-service", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> portfolioServiceFallback() {
        return buildResponse("Portfolio Service is currently unavailable");
    }

    @GetMapping(value = "/mail-service", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> mailServiceFallback() {
        return buildResponse("Mail Service is currently unavailable");
    }

    @GetMapping(value = "/customer-service", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> customerServiceFallback() {
        return buildResponse("Customer Service is currently unavailable");
    }
    @GetMapping(value = "/market-data-service", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> marketDataServiceFallback() {
        return buildResponse("Market Data Service is currently unavailable");
    }
    @GetMapping("/order-service")
    public  Mono<Map<String, Object>> orderServiceFallback() {
        return buildResponse("Order Service is currently unavailable");
    }

    private Mono<Map<String, Object>> buildResponse(String message) {
        return Mono.just(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", message,
                "path", "/fallback"
        ));
    }
}
