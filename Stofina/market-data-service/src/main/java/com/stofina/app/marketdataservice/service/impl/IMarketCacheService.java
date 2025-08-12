package com.stofina.app.marketdataservice.service.impl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Service interface for managing market data caching operations.
 * Provides reactive methods for caching and retrieving market data,
 * as well as managing WebSocket subscriptions.
 */
public interface IMarketCacheService {

    /**
     * Caches the current price for a stock symbol.
     *
     * @param symbol the stock symbol
     * @param price the current price to cache
     * @return Mono<Void> indicating completion
     */
    Mono<Void> cachePrice(String symbol, BigDecimal price);

    /**
     * Retrieves the cached price for a stock symbol.
     *
     * @param symbol the stock symbol
     * @return Mono containing the cached price if available
     */
    Mono<BigDecimal> getCachedPrice(String symbol);

    /**
     * Caches the daily high and low prices for a stock symbol.
     *
     * @param symbol the stock symbol
     * @param high the daily high price
     * @param low the daily low price
     * @return Mono<Void> indicating completion
     */
    Mono<Void> cacheDailyStats(String symbol, BigDecimal high, BigDecimal low);

    /**
     * Adds a WebSocket subscriber for a stock symbol.
     *
     * @param symbol the stock symbol to subscribe to
     * @param sessionId the WebSocket session ID
     * @return Mono<Void> indicating completion
     */
    Mono<Void> addSubscriber(String symbol, String sessionId);

    /**
     * Removes a WebSocket subscriber for a stock symbol.
     *
     * @param symbol the stock symbol to unsubscribe from
     * @param sessionId the WebSocket session ID to remove
     * @return Mono<Void> indicating completion
     */
    Mono<Void> removeSubscriber(String symbol, String sessionId);

    /**
     * Gets all WebSocket subscribers for a stock symbol.
     *
     * @param symbol the stock symbol
     * @return Flux of session IDs for all subscribers
     */
    Flux<String> getSubscribers(String symbol);

    /**
     * Clears all cached market data.
     *
     * @return Mono<Void> indicating completion
     */
    Mono<Void> clearAllCache();
}
