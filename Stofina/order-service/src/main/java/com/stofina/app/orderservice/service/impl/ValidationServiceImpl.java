package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.service.ValidationService;
import com.stofina.app.orderservice.service.client.MarketDataClient;
import com.stofina.app.orderservice.service.client.PortfolioClient;
import com.stofina.app.orderservice.dto.portfolio.PortfolioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final MarketDataClient marketDataClient;
    private final PortfolioClient portfolioClient;

    @Override
    public void validateOrderRequest(CreateOrderRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (request.getOrderType().requiresPrice() &&
                (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Price must be provided for this order type");
        }
    }

    @Override
    public boolean checkMarketHours() {
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Istanbul"));
        LocalTime open = LocalTime.of(9, 30);
        LocalTime close = LocalTime.of(18, 0);
        return !now.isBefore(open) && !now.isAfter(close);
    }

    @Override
    public void checkPriceLimits(String symbol, BigDecimal price) {
        // Get real-time market price
        BigDecimal currentPrice = marketDataClient.getCurrentPrice(symbol);
        if (currentPrice == null) {
            throw new IllegalArgumentException("Market fiyatı alınamadı: " + symbol);
        }
        
        // Use MarketDataClient's validatePriceInRange for ±1.5% check
        if (!marketDataClient.validatePriceInRange(symbol, price)) {
            BigDecimal deviation = price.subtract(currentPrice).abs()
                    .divide(currentPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                    
            throw new IllegalArgumentException(
                String.format("Fiyat ±%%1.5 limit dışı. Mevcut: %s TL, Girilen: %s TL, Sapma: %%%.2f", 
                             currentPrice, price, deviation));
        }
    }

    @Override
    public CompletableFuture<Void> checkAccountBalance(Long accountId, BigDecimal requiredAmount) {
        log.info("Validating account balance → AccountId: {}, RequiredAmount: {}", accountId, requiredAmount);
        
        return portfolioClient.validateAccountBalance(accountId, requiredAmount)
                .thenAccept(response -> {
                    if (!response.isSuccess()) {
                        log.error("Balance validation failed → AccountId: {}, Error: {}", accountId, response.getMessage());
                        throw new IllegalArgumentException(response.getMessage());
                    }
                    log.info("Balance validation passed → AccountId: {}", accountId);
                })
                .exceptionally(throwable -> {
                    log.error("Balance validation error → AccountId: {}, Error: {}", accountId, throwable.getMessage());
                    throw new IllegalArgumentException("Portfolio service validation failed: " + throwable.getMessage());
                });
    }

    @Override
    public CompletableFuture<Void> checkAccountPosition(Long accountId, String symbol, Integer quantity) {
        log.info("Validating stock position → AccountId: {}, Symbol: {}, RequiredQuantity: {}", accountId, symbol, quantity);
        
        return portfolioClient.validateStockPosition(accountId, symbol, quantity)
                .thenAccept(response -> {
                    if (!response.isSuccess()) {
                        log.error("Stock position validation failed → AccountId: {}, Symbol: {}, Error: {}", 
                                accountId, symbol, response.getMessage());
                        throw new IllegalArgumentException(response.getMessage());
                    }
                    log.info("Stock position validation passed → AccountId: {}, Symbol: {}", accountId, symbol);
                })
                .exceptionally(throwable -> {
                    log.error("Stock position validation error → AccountId: {}, Symbol: {}, Error: {}", 
                            accountId, symbol, throwable.getMessage());
                    throw new IllegalArgumentException("Portfolio service validation failed: " + throwable.getMessage());
                });
    }

    @Override
    public void validateOrderUpdate(Order existing, UpdateOrderRequest request) {
        if (request.getPrice() == null && request.getQuantity() == null) {
            throw new IllegalArgumentException("At least one field (price or quantity) must be provided for update");
        }
    }

}