package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.portfolio.CompensationType;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;

import java.math.BigDecimal;

/**
 * Service for handling Portfolio Service compensation operations.
 * 
 * This service is responsible for maintaining data consistency between
 * Order Service and Portfolio Service when trade confirmations fail or 
 * system errors occur that require rollback operations.
 * 
 * Compensation scenarios:
 * 1. Trade confirmation failure - rollback portfolio changes
 * 2. Order cancellation after partial execution - adjust reservations
 * 3. System errors during trade processing - restore consistent state
 * 4. Manual intervention required - flag for admin review
 */
public interface CompensationService {

    /**
     * Compensate for a failed trade confirmation.
     * This method is called when a trade was executed in Order Service
     * but the Portfolio Service confirmation failed.
     * 
     * @param trade The trade that failed confirmation
     * @param buyOrder The buy side order
     * @param sellOrder The sell side order
     * @param failureReason The reason why confirmation failed
     * @return true if compensation successful, false if manual intervention required
     */
    boolean compensateFailedTrade(Trade trade, Order buyOrder, Order sellOrder, String failureReason);

    /**
     * Compensate for a failed order reservation.
     * Called when order creation fails after portfolio reservation was made.
     * 
     * @param order The order that failed creation
     * @param reservationAmount The amount that was reserved (for buy orders)
     * @param reservationQuantity The quantity that was reserved (for sell orders)  
     * @param failureReason The reason why order creation failed
     * @return true if compensation successful, false if manual intervention required
     */
    boolean compensateFailedReservation(Order order, BigDecimal reservationAmount, 
                                      Integer reservationQuantity, String failureReason);

    /**
     * Compensate for a partial trade that left the system in inconsistent state.
     * Called when partial trade confirmation fails and portfolio state is unclear.
     * 
     * @param trade The partial trade that needs compensation
     * @param order The order involved in partial trade
     * @param executedQuantity The quantity that was actually executed
     * @param failureReason The reason for compensation
     * @return true if compensation successful, false if manual intervention required
     */
    boolean compensatePartialTrade(Trade trade, Order order, Integer executedQuantity, String failureReason);

    /**
     * Handle critical compensation failures that require immediate attention.
     * This method logs critical errors and sends alerts to system administrators.
     * 
     * @param compensationType The type of compensation that failed
     * @param orderId The order ID involved
     * @param tradeId The trade ID involved (nullable)
     * @param errorDetails Technical details of the failure
     * @param affectedAccounts Account IDs that may be affected
     */
    void handleCriticalCompensationFailure(CompensationType compensationType, Long orderId, 
                                         Long tradeId, String errorDetails, Long... affectedAccounts);

    /**
     * Get the current compensation queue status.
     * Returns information about pending compensations that require processing.
     * 
     * @return compensation queue status information
     */
    CompensationQueueStatus getCompensationQueueStatus();

    /**
     * Retry failed compensations that are eligible for automatic retry.
     * This method processes compensations that previously failed but can be retried.
     * 
     * @return number of compensations successfully retried
     */
    int retryFailedCompensations();

    /**
     * Check if Portfolio Service is healthy enough to perform compensations.
     * Used to determine if compensation operations should be attempted.
     * 
     * @return true if Portfolio Service is healthy for compensations
     */
    boolean isPortfolioServiceHealthyForCompensations();

    /**
     * Inner class for compensation queue status information.
     */
    class CompensationQueueStatus {
        private final int pendingCount;
        private final int failedCount;
        private final int criticalCount;
        private final int retryableCount;

        public CompensationQueueStatus(int pendingCount, int failedCount, 
                                     int criticalCount, int retryableCount) {
            this.pendingCount = pendingCount;
            this.failedCount = failedCount;
            this.criticalCount = criticalCount;
            this.retryableCount = retryableCount;
        }

        public int getPendingCount() { return pendingCount; }
        public int getFailedCount() { return failedCount; }
        public int getCriticalCount() { return criticalCount; }
        public int getRetryableCount() { return retryableCount; }
        
        public boolean hasCriticalFailures() { return criticalCount > 0; }
        public boolean hasRetryableCompensations() { return retryableCount > 0; }
        
        @Override
        public String toString() {
            return String.format("CompensationQueue[pending=%d, failed=%d, critical=%d, retryable=%d]", 
                    pendingCount, failedCount, criticalCount, retryableCount);
        }
    }
}