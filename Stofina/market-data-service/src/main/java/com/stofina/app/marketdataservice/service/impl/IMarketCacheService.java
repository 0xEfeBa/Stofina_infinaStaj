package com.stofina.app.marketdataservice.service.impl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface IMarketCacheService {

    Mono<Void> cachePrice(String symbol, BigDecimal price);

    Mono<BigDecimal> getCachedPrice(String symbol);

    Mono<Void> cacheDailyStats(String symbol, BigDecimal high, BigDecimal low);

    Mono<Void> addSubscriber(String symbol, String sessionId);

    Mono<Void> removeSubscriber(String symbol, String sessionId);

    Flux<String> getSubscribers(String symbol);

    Mono<Void> clearAllCache();
}
