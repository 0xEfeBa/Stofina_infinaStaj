package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.entity.Order;

import java.math.BigDecimal;

public interface ValidationService {

    void validateOrderRequest(CreateOrderRequest request);

    boolean checkMarketHours();

    void checkPriceLimits(String symbol, BigDecimal price);

    void checkAccountBalance(Long accountId, BigDecimal requiredAmount);

    void checkAccountPosition(Long accountId, String symbol, BigDecimal quantity);

    void validateOrderUpdate(com.stofina.app.orderservice.entity.Order existing, UpdateOrderRequest request);
}
