package com.stofina.app.apigateway.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class RedisRateLimiterGatewayFilter implements GlobalFilter {

    private final StringRedisTemplate redisTemplate;
    private static final int REQUEST_LIMIT = 10;
    private static final int WINDOW_SECONDS = 1;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String ip = extractClientIp(exchange);
        String redisKey = "rl:" + ip;

        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Long count = ops.increment(redisKey);

        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (count != null && count > REQUEST_LIMIT) {
            log.warn("Too many requests from IP {}. Count: {}", ip, count);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String body = """
                {
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "You have exceeded the allowed number of requests. Please try again later."
                }
                """;
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
        }

        return chain.filter(exchange);
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null) return forwardedFor.split(",")[0].trim();
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
