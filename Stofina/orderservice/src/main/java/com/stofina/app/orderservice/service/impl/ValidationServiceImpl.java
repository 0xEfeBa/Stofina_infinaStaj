package com.stofina.orderservice.service.impl;

import com.stofina.orderservice.dto.request.CreateOrderRequest;
import com.stofina.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.service.ValidationService;
import com.stofina.orderservice.service.client.MarketDataClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final MarketDataClient marketDataClient;

    private static final BigDecimal DEFAULT_ACCOUNT_BALANCE = new BigDecimal("100000");
    private static final BigDecimal DEFAULT_ACCOUNT_POSITION = new BigDecimal("1000");
    private static final BigDecimal DAILY_LIMIT_PERCENT = new BigDecimal("0.10");

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
        // TODO: Implement proper price limit checking
        BigDecimal referencePrice = new BigDecimal("100"); // Mock reference price
        BigDecimal upperLimit = referencePrice.add(referencePrice.multiply(DAILY_LIMIT_PERCENT));
        BigDecimal lowerLimit = referencePrice.subtract(referencePrice.multiply(DAILY_LIMIT_PERCENT));

        if (price.compareTo(upperLimit) > 0 || price.compareTo(lowerLimit) < 0) {
            throw new IllegalArgumentException("Price is out of daily limit range for " + symbol);
        }
    }

    @Override
    public void checkAccountBalance(Long accountId, BigDecimal requiredAmount) {
        if (requiredAmount.compareTo(DEFAULT_ACCOUNT_BALANCE) > 0) {
            throw new IllegalArgumentException("Insufficient balance for account " + accountId);
        }
    }

    @Override
    public void checkAccountPosition(Long accountId, String symbol, BigDecimal quantity) {
        if (quantity.compareTo(DEFAULT_ACCOUNT_POSITION) > 0) {
            throw new IllegalArgumentException("Insufficient position for account " + accountId + " on " + symbol);
        }
    }

    @Override
    public void validateOrderUpdate(Order existing, UpdateOrderRequest request) {
        if (request.getPrice() == null && request.getQuantity() == null) {
            throw new IllegalArgumentException("At least one field (price or quantity) must be provided for update");
        }
    }
}