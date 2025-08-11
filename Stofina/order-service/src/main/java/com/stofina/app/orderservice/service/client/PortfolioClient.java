package com.stofina.app.orderservice.service.client;

import com.stofina.app.orderservice.dto.portfolio.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Client interface for communicating with Portfolio Service.
 * Defines all operations that Order Service can perform on Portfolio Service.
 * 
 * This interface follows the contract established by Portfolio Service endpoints
 * and provides type-safe methods for all portfolio operations.
 */
public interface PortfolioClient {

    // ========================================
    // PHASE 1: BASIC RESERVATIONS
    // ========================================

    /**
     * Reserve funds for a buy stock order.
     * Called when a buy order is created to ensure sufficient balance.
     * 
     * @param request Buy stock reservation request
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> reserveBuyStock(BuyStockRequest request);

    /**
     * Reserve stock for a sell stock order.
     * Called when a sell order is created to ensure sufficient stock quantity.
     * 
     * @param request Sell stock reservation request  
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> reserveSellStock(SellStockRequest request);

    // ========================================
    // PHASE 2: TRADE CONFIRMATIONS
    // ========================================

    /**
     * Confirm a completed buy trade execution.
     * Called when a buy order is fully executed to update portfolio.
     * 
     * @param request Trade confirmation request for buy side
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> confirmBuyTrade(TradeConfirmationRequest request);

    /**
     * Confirm a completed sell trade execution.
     * Called when a sell order is fully executed to update portfolio.
     * 
     * @param request Trade confirmation request for sell side
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> confirmSellTrade(TradeConfirmationRequest request);

    /**
     * Confirm a partial buy trade execution.
     * Called when a buy order is partially filled to update portfolio.
     * 
     * @param request Partial trade confirmation request for buy side
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> confirmPartialBuyTrade(PartialTradeConfirmationRequest request);

    /**
     * Confirm a partial sell trade execution.
     * Called when a sell order is partially filled to update portfolio.
     * 
     * @param request Partial trade confirmation request for sell side
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> confirmPartialSellTrade(PartialTradeConfirmationRequest request);

    // ========================================
    // PHASE 3: ORDER CANCELLATIONS
    // ========================================

    /**
     * Cancel a buy order and release reserved funds.
     * Called when a buy order is cancelled to unreserve balance.
     * 
     * @param request Order cancellation request for buy side
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> cancelBuyOrder(OrderCancellationRequest request);

    /**
     * Cancel a sell order and release reserved stock.
     * Called when a sell order is cancelled to unreserve stock.
     * 
     * @param request Order cancellation request for sell side
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> cancelSellOrder(OrderCancellationRequest request);

    // ========================================
    // PHASE 4: ADVANCED OPERATIONS (Future Implementation)
    // ========================================

    /**
     * Perform compensation operation for failed trades.
     * Used to maintain data consistency when trade confirmations fail.
     * 
     * @param request Compensation request details
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> compensateTrade(CompensationRequest request);

    /**
     * Validate account balance before order creation.
     * Optional validation method for additional safety checks.
     * 
     * @param accountId Account ID to validate
     * @param requiredAmount Required balance amount
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> validateAccountBalance(Long accountId, BigDecimal requiredAmount);

    /**
     * Validate stock position before order creation.
     * Optional validation method for additional safety checks.
     * 
     * @param accountId Account ID to validate
     * @param symbol Stock symbol
     * @param requiredQuantity Required stock quantity
     * @return CompletableFuture with portfolio response
     */
    CompletableFuture<PortfolioResponse> validateStockPosition(Long accountId, String symbol, Integer requiredQuantity);

    // ========================================
    // HEALTH & MONITORING
    // ========================================

    /**
     * Check if Portfolio Service is healthy and available.
     * Used for circuit breaker and health monitoring.
     * 
     * @return CompletableFuture with health status (true if healthy)
     */
    CompletableFuture<Boolean> isPortfolioServiceHealthy();
}