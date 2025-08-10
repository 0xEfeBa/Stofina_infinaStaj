package com.stofina.app.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // USER-SERVICE ROUTES
                .route(r -> r
                        .path(
                                "/api/v1/users/**",
                                "/api/v1/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/(?<segment>.*)", "/api/v1/${segment}")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,HttpMethod.DELETE)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .circuitBreaker(cb -> cb
                                        .setName("userCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service")))
                        .uri("lb://USER-SERVICE"))

                // CUSTOMER-SERVICE ROUTES
                .route(r -> r
                        .path(
                                "/api/v1/individual/**",
                                "/api/v1/corporate/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/(?<segment>.*)", "/api/v1/${segment}")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,HttpMethod.DELETE)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .circuitBreaker(cb -> cb
                                        .setName("customerCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/customer-service")))
                        .uri("lb://CUSTOMER-SERVICE"))

                // MAIL-SERVICE ROUTES
                .route(r -> r
                        .path("/api/v1/mails/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/mails/(?<segment>.*)", "/api/v1/mails/${segment}")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,HttpMethod.DELETE)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .circuitBreaker(cb -> cb
                                        .setName("mailCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/mail-service")))
                        .uri("lb://MAIL-SERVICE"))

                // PORTFOLIO-SERVICE ROUTES
                .route(r -> r
                    .path(
                            "/api/v1/stocks/**",
                            "/api/v1/accounts/**",
                            "/api/v1/transactions/**",
                            "/api/v1/balances/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/(?<segment>.*)", "/api/v1/${segment}")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,HttpMethod.DELETE)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .circuitBreaker(cb -> cb
                                        .setName("portfolioCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/portfolio-service")))
                        .uri("lb://PORTFOLIO-SERVICE"))

                // MARKET-DATA-SERVICE ROUTES
                .route(r -> r
                        .path(
                                "/api/v1/market/**",
                                "/market/send-stock",
                                "/market/send-all",
                                "/ws/**"
                        )
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .circuitBreaker(cb -> cb
                                        .setName("marketDataCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/market-data-service"))
                        )
                        .uri("lb://MARKET-DATA-SERVICE"))
                // ORDER-SERVICE ROUTES
                .route(r -> r
                        .path(
                                "/api/v1/orders/**",
                                "/api/v1/market/**",
                                "/api/stop-loss/**",
                                "/api/v1/orderbook/**"
                        )
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.DELETE)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order-service"))
                        )
                        .uri("lb://ORDER-SERVICE"))

                .build();
    }
}
