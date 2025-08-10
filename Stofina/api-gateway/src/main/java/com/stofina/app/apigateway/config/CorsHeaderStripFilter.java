package com.stofina.app.apigateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

@Configuration
public class CorsHeaderStripFilter {

    @Bean
    public GlobalFilter stripAndSetCorsHeaders() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse res = exchange.getResponse();
            HttpHeaders h = res.getHeaders();

            h.remove("Access-Control-Allow-Origin");
            h.remove("access-control-allow-origin");
            h.remove("Access-Control-Allow-Credentials");
            h.remove("access-control-allow-credentials");
            h.remove("Access-Control-Expose-Headers");
            h.remove("access-control-expose-headers");

            String origin = exchange.getRequest().getHeaders().getOrigin();
            if (origin != null && !origin.isBlank()) {
                h.set("Access-Control-Allow-Origin", origin);
                h.set("Access-Control-Allow-Credentials", "true");
                h.set("Access-Control-Expose-Headers", "Authorization, Content-Type");
            }

            h.addIfAbsent("Vary", "Origin");
        }));
    }
}
