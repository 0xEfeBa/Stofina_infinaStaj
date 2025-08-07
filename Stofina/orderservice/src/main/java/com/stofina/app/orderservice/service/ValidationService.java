package com.stofina.app.orderservice.service;

import com.stofina.orderservice.dto.request.CreateOrderRequest;
import com.stofina.orderservice.dto.request.UpdateOrderRequest;

import java.math.BigDecimal;

public interface ValidationService {

    void validateOrderRequest(CreateOrderRequest request);

    boolean checkMarketHours();

    void checkPriceLimits(String symbol, BigDecimal price);

    void checkAccountBalance(Long accountId, BigDecimal requiredAmount);

    void checkAccountPosition(Long accountId, String symbol, BigDecimal quantity);

    void validateOrderUpdate(Order existing, UpdateOrderRequest request);
}
