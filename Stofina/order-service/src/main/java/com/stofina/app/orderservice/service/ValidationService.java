package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.request.CreateOrderRequest;
import com.stofina.app.orderservice.dto.request.UpdateOrderRequest;
import com.stofina.app.orderservice.entity.Order;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for validating order-related operations.
 * Provides methods for validating orders, market conditions, and account status.
 */
public interface ValidationService {

    /**
     * Validates a new order request for compliance with business rules.
     *
     * @param request the order request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    void validateOrderRequest(CreateOrderRequest request);

    /**
     * Checks if the market is currently open for trading.
     *
     * @return true if market is open, false otherwise
     */
    boolean checkMarketHours();

    /**
     * Validates if the given price is within allowed limits for the symbol.
     *
     * @param symbol the stock symbol
     * @param price the price to validate
     * @throws IllegalArgumentException if price is outside allowed limits
     */
    void checkPriceLimits(String symbol, BigDecimal price);

    /**
     * Asynchronously checks if an account has sufficient balance for a transaction.
     *
     * @param accountId the account ID to check
     * @param requiredAmount the amount needed for the transaction
     * @return CompletableFuture that completes when validation is done
     */
    CompletableFuture<Void> checkAccountBalance(Long accountId, BigDecimal requiredAmount);

    /**
     * Asynchronously checks if an account has sufficient position for a sell order.
     *
     * @param accountId the account ID to check
     * @param symbol the stock symbol
     * @param quantity the quantity to validate
     * @return CompletableFuture that completes when validation is done
     */
    CompletableFuture<Void> checkAccountPosition(Long accountId, String symbol, Integer quantity);

    /**
     * Validates an order update request against the existing order.
     *
     * @param existing the existing order
     * @param request the update request
     * @throws IllegalArgumentException if the update is invalid
     */
    void validateOrderUpdate(Order existing, UpdateOrderRequest request);
}
