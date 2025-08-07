package com.stofina.app.orderservice.service.client.impl;


import com.stofina.orderservice.config.MarketDataConfig;
import com.stofina.orderservice.dto.response.external.PriceResponse;
import com.stofina.orderservice.service.client.MarketDataClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;


@Service
public class MarketDataClientImpl implements MarketDataClient {

    private final WebClient webClient;
    private final MarketDataConfig marketDataConfig;
    private final RedisTemplate<String, BigDecimal> redisTemplate;

    public MarketDataClientImpl(WebClient webClient, MarketDataConfig marketDataConfig, RedisTemplate<String, BigDecimal> redisTemplate) {
        this.webClient = webClient;
        this.marketDataConfig = marketDataConfig;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        String endpoint = marketDataConfig.getEndpoints().get("price");
        String url = endpoint.replace("{symbol}", symbol);

        // Önce cacheden dene
        BigDecimal cachedPrice = getCachedPrice(symbol);
        if (cachedPrice != null) {
            return cachedPrice;
        }

        PriceResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(PriceResponse.class)
                .block(Duration.ofSeconds(5));

        if (response != null) {
            setCachedPrice(symbol, response.getPrice());
            return response.getPrice();
        }
        return null;
    }

    @Override
    public PriceResponse getPriceWithDetails(String symbol) {
        String endpoint = marketDataConfig.getEndpoints().get("price");
        String url = endpoint.replace("{symbol}", symbol);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(PriceResponse.class)
                .block(Duration.ofSeconds(5));
    }

    @Override
    public List<String> getSymbolList() {
        String endpoint = marketDataConfig.getEndpoints().get("symbols");

        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .block(Duration.ofSeconds(5));
    }

    @Override
    public boolean isMarketOpen() {
        String endpoint = marketDataConfig.getEndpoints().get("market-hours");

        var response = webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(PriceResponse.class)
                .block(Duration.ofSeconds(5));

        return response != null && response.isMarketOpen();
    }

    @Override
    public boolean validatePriceInRange(String symbol, BigDecimal price) {
        BigDecimal currentPrice = getCurrentPrice(symbol);
        if (currentPrice == null || price == null) {
            return false;
        }
        // %10 limit kontrolü
        BigDecimal deviation = price.subtract(currentPrice).abs().divide(currentPrice, 6, BigDecimal.ROUND_HALF_UP);
        return deviation.compareTo(new BigDecimal("0.10")) <= 0;
    }

    @Override
    public boolean healthCheck() {
        String endpoint = marketDataConfig.getEndpoints().get("health");

        try {
            var response = webClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(3));

            return response != null && response.contains("UP");
        } catch (Exception e) {
            return false;
        }
    }

    // Cache getter/setter
    public BigDecimal getCachedPrice(String cacheKey) {
        return redisTemplate.opsForValue().get(cacheKey);
    }

    public void setCachedPrice(String cacheKey, BigDecimal price) {
        redisTemplate.opsForValue().set(cacheKey, price, Duration.ofMinutes(5));
    }

    // Gerektiğinde request build edilebilir (daha genel)
    private <T> Mono<T> buildRequest(String endpoint, Class<T> responseType, Object... params) {
        String url = String.format(endpoint, params);
        return webClient.get().uri(url).retrieve().bodyToMono(responseType);
    }
}
