package com.stofina.app.apigateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
public class ResponseTraceFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseTraceFilter.class);
    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);
                if (correlationId != null) {
                    logger.debug("Setting Correlation ID in Response: {}", correlationId);
                    exchange.getResponse().getHeaders().add(CORRELATION_ID, correlationId);
                }
            }));
        };
    }
}
