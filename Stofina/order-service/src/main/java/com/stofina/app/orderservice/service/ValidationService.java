package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface ValidationService {

    void validateOrderRequest(CreateOrderRequest request);

    boolean checkMarketHours();

    void checkPriceLimits(String symbol, BigDecimal price);

    CompletableFuture<Void> checkAccountBalance(Long accountId, BigDecimal requiredAmount);

    CompletableFuture<Void> checkAccountPosition(Long accountId, String symbol, Integer quantity);

    void validateOrderUpdate(Order existing, UpdateOrderRequest request);
}
