package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.dto.portfolio.CompensationRequest;
import com.stofina.app.orderservice.dto.portfolio.CompensationType;
import com.stofina.app.orderservice.dto.portfolio.PortfolioResponse;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.service.CompensationService;
import com.stofina.app.orderservice.service.client.PortfolioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of CompensationService for handling Portfolio Service compensation operations.
 * 
 * This service maintains data consistency between Order Service and Portfolio Service
 * by implementing compensation patterns for failed operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationServiceImpl implements CompensationService {

    private final PortfolioClient portfolioClient;
    
    // Compensation queue counters (in production, these would be persisted)
    private final AtomicInteger pendingCompensations = new AtomicInteger(0);
    private final AtomicInteger failedCompensations = new AtomicInteger(0);
    private final AtomicInteger criticalCompensations = new AtomicInteger(0);
    private final AtomicInteger retryableCompensations = new AtomicInteger(0);

    @Override
    public boolean compensateFailedTrade(Trade trade, Order buyOrder, Order sellOrder, String failureReason) {
        log.error("🔧 COMPENSATION: Trade confirmation failed → TradeId: {}, BuyOrder: {}, SellOrder: {}, Reason: {}", 
                trade.getTradeId(), buyOrder.getOrderId(), sellOrder.getOrderId(), failureReason);

        try {
            pendingCompensations.incrementAndGet();

            // Create compensation request for trade rollback
            CompensationRequest compensationRequest = CompensationRequest.forTradeRollback(
                    trade.getBuyOrderId(), // Use buy order as primary order for compensation
                    trade.getTradeId(),
                    trade.getPrice().multiply(trade.getQuantity()), // Total trade value
                    trade.getQuantity().intValue(),
                    failureReason
            );

            log.info("🔧 COMPENSATION: Attempting trade rollback → {}", compensationRequest.getSummary());

            CompletableFuture<PortfolioResponse> compensationFuture = portfolioClient.compensateTrade(compensationRequest);
            PortfolioResponse response = compensationFuture.get();

            if (response.isSuccess()) {
                log.info("✅ COMPENSATION: Trade rollback successful → TradeId: {}, Message: {}", 
                        trade.getTradeId(), response.getMessage());
                pendingCompensations.decrementAndGet();
                return true;
            } else {
                log.error("❌ COMPENSATION: Trade rollback failed → TradeId: {}, Error: {}", 
                        trade.getTradeId(), response.getMessage());
                
                failedCompensations.incrementAndGet();
                pendingCompensations.decrementAndGet();
                
                // Check if this is a critical failure
                if (isCompensationCritical(response.getErrorCode())) {
                    handleCriticalCompensationFailure(
                            CompensationType.TRADE_ROLLBACK,
                            buyOrder.getOrderId(),
                            trade.getTradeId(),
                            response.getMessage(),
                            buyOrder.getAccountId(),
                            sellOrder.getAccountId()
                    );
                } else {
                    // Mark as retryable for later processing
                    retryableCompensations.incrementAndGet();
                }
                
                return false;
            }

        } catch (ExecutionException e) {
            log.error("🔧 COMPENSATION: Trade rollback execution error → TradeId: {}", trade.getTradeId(), e);
            failedCompensations.incrementAndGet();
            pendingCompensations.decrementAndGet();
            
            handleCriticalCompensationFailure(
                    CompensationType.TRADE_ROLLBACK,
                    buyOrder.getOrderId(),
                    trade.getTradeId(),
                    "Execution error: " + e.getMessage(),
                    buyOrder.getAccountId(),
                    sellOrder.getAccountId()
            );
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🔧 COMPENSATION: Trade rollback interrupted → TradeId: {}", trade.getTradeId(), e);
            failedCompensations.incrementAndGet();
            pendingCompensations.decrementAndGet();
            return false;

        } catch (Exception e) {
            log.error("🔧 COMPENSATION: Unexpected error during trade rollback → TradeId: {}", trade.getTradeId(), e);
            failedCompensations.incrementAndGet();
            pendingCompensations.decrementAndGet();
            
            handleCriticalCompensationFailure(
                    CompensationType.TRADE_ROLLBACK,
                    buyOrder.getOrderId(),
                    trade.getTradeId(),
                    "Unexpected error: " + e.getMessage(),
                    buyOrder.getAccountId(),
                    sellOrder.getAccountId()
            );
            return false;
        }
    }

    @Override
    public boolean compensateFailedReservation(Order order, BigDecimal reservationAmount, 
                                             Integer reservationQuantity, String failureReason) {
        log.error("🔧 COMPENSATION: Order reservation compensation needed → OrderId: {}, Amount: {}, Quantity: {}, Reason: {}", 
                order.getOrderId(), reservationAmount, reservationQuantity, failureReason);

        try {
            pendingCompensations.incrementAndGet();

            // Create compensation request for reservation cancellation
            BigDecimal compensationAmount = reservationAmount != null ? reservationAmount : BigDecimal.ZERO;
            
            CompensationRequest compensationRequest = CompensationRequest.forReservationCancellation(
                    order.getOrderId(),
                    compensationAmount,
                    failureReason
            );

            log.info("🔧 COMPENSATION: Attempting reservation cancellation → {}", compensationRequest.getSummary());

            CompletableFuture<PortfolioResponse> compensationFuture = portfolioClient.compensateTrade(compensationRequest);
            PortfolioResponse response = compensationFuture.get();

            if (response.isSuccess()) {
                log.info("✅ COMPENSATION: Reservation cancellation successful → OrderId: {}", order.getOrderId());
                pendingCompensations.decrementAndGet();
                return true;
            } else {
                log.error("❌ COMPENSATION: Reservation cancellation failed → OrderId: {}, Error: {}", 
                        order.getOrderId(), response.getMessage());
                
                failedCompensations.incrementAndGet();
                pendingCompensations.decrementAndGet();
                
                if (isCompensationCritical(response.getErrorCode())) {
                    handleCriticalCompensationFailure(
                            CompensationType.RESERVATION_CANCELLATION,
                            order.getOrderId(),
                            null,
                            response.getMessage(),
                            order.getAccountId()
                    );
                } else {
                    retryableCompensations.incrementAndGet();
                }
                
                return false;
            }

        } catch (Exception e) {
            log.error("🔧 COMPENSATION: Failed reservation compensation error → OrderId: {}", order.getOrderId(), e);
            failedCompensations.incrementAndGet();
            pendingCompensations.decrementAndGet();
            
            handleCriticalCompensationFailure(
                    CompensationType.RESERVATION_CANCELLATION,
                    order.getOrderId(),
                    null,
                    "Exception: " + e.getMessage(),
                    order.getAccountId()
            );
            return false;
        }
    }

    @Override
    public boolean compensatePartialTrade(Trade trade, Order order, Integer executedQuantity, String failureReason) {
        log.error("🔧 COMPENSATION: Partial trade compensation needed → TradeId: {}, OrderId: {}, ExecutedQuantity: {}, Reason: {}", 
                trade.getTradeId(), order.getOrderId(), executedQuantity, failureReason);

        // For now, treat partial trade compensation similar to full trade rollback
        // In production, this might require more sophisticated logic
        Order dummyOppositeOrder = createDummyOppositeOrder(order, trade);
        return compensateFailedTrade(trade, 
                order.getSide().name().equals("BUY") ? order : dummyOppositeOrder,
                order.getSide().name().equals("SELL") ? order : dummyOppositeOrder,
                "Partial trade compensation: " + failureReason);
    }

    @Override
    public void handleCriticalCompensationFailure(CompensationType compensationType, Long orderId, 
                                                 Long tradeId, String errorDetails, Long... affectedAccounts) {
        criticalCompensations.incrementAndGet();
        
        log.error("🚨 CRITICAL COMPENSATION FAILURE: Type: {}, OrderId: {}, TradeId: {}, Accounts: {}", 
                compensationType, orderId, tradeId, java.util.Arrays.toString(affectedAccounts));
        log.error("🚨 CRITICAL ERROR DETAILS: {}", errorDetails);

        // In production, this would:
        // 1. Send alerts to system administrators
        // 2. Create database records for manual review
        // 3. Send notifications to affected accounts
        // 4. Create support tickets
        // 5. Log to monitoring systems (like PagerDuty, Slack, etc.)
        
        String alertMessage = String.format(
                "CRITICAL COMPENSATION FAILURE: %s for Order %d%s affecting accounts %s. Details: %s",
                compensationType, orderId, 
                tradeId != null ? " (Trade " + tradeId + ")" : "",
                java.util.Arrays.toString(affectedAccounts),
                errorDetails
        );
        
        // TODO: Implement actual alerting mechanism
        log.error("🚨 ALERT WOULD BE SENT: {}", alertMessage);
    }

    @Override
    public CompensationQueueStatus getCompensationQueueStatus() {
        return new CompensationQueueStatus(
                pendingCompensations.get(),
                failedCompensations.get(),
                criticalCompensations.get(),
                retryableCompensations.get()
        );
    }

    @Override
    public int retryFailedCompensations() {
        int retryableCount = retryableCompensations.get();
        if (retryableCount == 0) {
            log.debug("🔧 COMPENSATION: No retryable compensations found");
            return 0;
        }

        log.info("🔧 COMPENSATION: Attempting to retry {} failed compensations", retryableCount);
        
        // In production, this would:
        // 1. Retrieve failed compensations from database
        // 2. Check if Portfolio Service is healthy
        // 3. Retry each compensation
        // 4. Update compensation status
        
        // For now, just reset the counter (mock implementation)
        int retriedCount = retryableCompensations.getAndSet(0);
        log.info("✅ COMPENSATION: Mock retry of {} compensations completed", retriedCount);
        
        return retriedCount;
    }

    @Override
    public boolean isPortfolioServiceHealthyForCompensations() {
        try {
            CompletableFuture<Boolean> healthFuture = portfolioClient.isPortfolioServiceHealthy();
            Boolean isHealthy = healthFuture.get();
            
            log.debug("🔧 COMPENSATION: Portfolio Service health check → Healthy: {}", isHealthy);
            return isHealthy != null && isHealthy;
            
        } catch (Exception e) {
            log.warn("🔧 COMPENSATION: Portfolio Service health check failed → {}", e.getMessage());
            return false;
        }
    }

    // PRIVATE HELPER METHODS

    /**
     * Determines if a compensation failure is critical and requires immediate attention.
     */
    private boolean isCompensationCritical(String errorCode) {
        return errorCode != null && (
                errorCode.contains("CRITICAL") ||
                errorCode.contains("CORRUPTION") ||
                errorCode.contains("INCONSISTENCY") ||
                errorCode.contains("MANUAL_INTERVENTION_REQUIRED")
        );
    }

    /**
     * Creates a dummy opposite order for partial trade compensation.
     * This is a temporary solution for the mock implementation.
     */
    private Order createDummyOppositeOrder(Order originalOrder, Trade trade) {
        // In production, this would retrieve the actual opposite order from database
        // For now, create a minimal dummy order for compensation purposes
        Order dummyOrder = new Order();
        dummyOrder.setOrderId(-1L); // Dummy ID to indicate this is not a real order
        dummyOrder.setAccountId(-1L); // This would be the actual opposite order's account
        dummyOrder.setSymbol(originalOrder.getSymbol());
        dummyOrder.setQuantity(trade.getQuantity());
        dummyOrder.setPrice(trade.getPrice());
        dummyOrder.setCreatedAt(LocalDateTime.now());
        
        return dummyOrder;
    }
}