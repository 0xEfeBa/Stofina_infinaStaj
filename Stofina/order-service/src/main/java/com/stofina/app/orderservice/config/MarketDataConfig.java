package com.stofina.app.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "market-data")
public class MarketDataConfig {

    private String baseUrl;
    private Map<String, String> endpoints;
    private ConnectionConfig connection;
    private RetryConfig retry;

    @Data
    public static class ConnectionConfig {
        private int timeout;
        private int maxConnections;
    }

    @Data
    public static class RetryConfig {
        private int maxAttempts;
        private long delay;
    }
}