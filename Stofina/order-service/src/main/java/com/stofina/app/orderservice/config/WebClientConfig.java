package com.stofina.app.orderservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${market-data.base-url}")
    private String marketData;

    @Value("${portfolio-service.base-url}")
    private String portfolioServiceUrl;

    @Value("${portfolio-service.connection.timeout:15000}")
    private int portfolioConnectionTimeout;

    @Value("${portfolio-service.connection.read-timeout:30000}")
    private int portfolioReadTimeout;

    @Value("${portfolio-service.connection.max-connections:100}")
    private int portfolioMaxConnections;

    @Value("${portfolio-service.connection.max-idle-time:60000}")
    private int portfolioMaxIdleTime;

    @Value("${portfolio-service.connection.pending-acquire-timeout:120000}")
    private int portfolioPendingAcquireTimeout;

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("marketDataConnectionPool")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .maxIdleTime(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    public HttpClient httpClient(ConnectionProvider connectionProvider) {
        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
                );
    }

    @Bean
    public WebClient webClient(HttpClient httpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(marketData)
                .build();
    }

    /**
     * Portfolio Service specific connection provider with optimized settings.
     * Separate from market data to allow independent configuration.
     */
    @Bean("portfolioConnectionProvider")
    public ConnectionProvider portfolioConnectionProvider() {
        return ConnectionProvider.builder("portfolioServiceConnectionPool")
                .maxConnections(portfolioMaxConnections)
                .pendingAcquireTimeout(Duration.ofMillis(portfolioPendingAcquireTimeout))
                .maxIdleTime(Duration.ofMillis(portfolioMaxIdleTime))
                .build();
    }

    /**
     * Portfolio Service specific HTTP client with dedicated timeout settings.
     */
    @Bean("portfolioHttpClient")
    public HttpClient portfolioHttpClient(@Qualifier("portfolioConnectionProvider") ConnectionProvider connectionProvider) {
        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, portfolioConnectionTimeout)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(portfolioReadTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(portfolioReadTimeout, TimeUnit.MILLISECONDS))
                );
    }

    /**
     * Portfolio Service dedicated WebClient with authentication and logging.
     * This client is specifically configured for Portfolio Service communication.
     */
    @Bean("portfolioWebClient")
    public WebClient portfolioWebClient(@Qualifier("portfolioHttpClient") HttpClient portfolioHttpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(portfolioHttpClient))
                .baseUrl(portfolioServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "OrderService/1.0")
                .filter((request, next) -> {
                    // Add authentication header from current request context
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest httpRequest = attributes.getRequest();
                        String authHeader = httpRequest.getHeader("Authorization");
                        if (authHeader != null) {
                            request = ClientRequest.from(request)
                                    .headers(headers -> headers.set("Authorization", authHeader))
                                    .build();
                        }
                    }

                    log.debug("Portfolio Service Request → {} {}", request.method(), request.url());
                    return next.exchange(request);
                })
                .filter((request, next) -> {
                    // Response logging and error handling
                    return next.exchange(request)
                            .doOnSuccess(response -> {
                                if (response.statusCode().isError()) {
                                    log.warn("Portfolio Service Error Response → {} {} returned {}",
                                            request.method(), request.url(), response.statusCode());
                                }
                            })
                            .doOnError(throwable -> {
                                log.error("Portfolio Service Request Failed → {} {} - Error: {}",
                                        request.method(), request.url(), throwable.getMessage());
                            });
                })
                .build();
    }

    @Bean
    public WebClientCustomizer webClientCustomizer() {
        return webClientBuilder -> webClientBuilder
                .defaultHeader("Content-Type", "application/json")
                .filter((request, next) -> {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest httpRequest = attributes.getRequest();
                        String authHeader = httpRequest.getHeader("Authorization");
                        if (authHeader != null) {
                            request = ClientRequest.from(request)
                                    .headers(headers -> headers.set("Authorization", authHeader))
                                    .build();
                        }
                    }

                    log.info("WebClient Request → {} {}", request.method(), request.url());
                    return next.exchange(request);
                });
    }

}

