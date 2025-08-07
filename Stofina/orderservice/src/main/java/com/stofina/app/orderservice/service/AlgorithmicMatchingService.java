package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;

import java.util.List;

public interface AlgorithmicMatchingService {
    
    // CHECKPOINT C1 - Algorithmic Matching Service Contract
    
    /**
     * Schedules algorithmic matching for an order that couldn't be fully matched
     * @param order The order that needs algorithmic matching
     * @param delaySeconds Delay before executing (15 seconds)
     */
    void scheduleAlgorithmicMatching(Order order, int delaySeconds);
    
    /**
     * Executes algorithmic matching with 3 strategies:
     * - FULL_FILL (30%): Generate full counter-order and match completely
     * - PARTIAL_FILL (40%): Generate partial counter-order and match partially  
     * - NO_FILL (30%): No matching, order stays in book
     * 
     * @param order The order to process algorithmically
     * @return List of trades if matching occurred
     */
    List<Trade> executeAlgorithmicMatching(Order order);
    
    /**
     * Checks if an order is eligible for algorithmic matching
     * (max 2 times per order)
     * @param orderId Order ID to check
     * @return true if eligible, false if already processed 2 times
     */
    boolean isEligibleForAlgorithmicMatching(Long orderId);
    
    /**
     * Gets current algorithmic matching count for an order
     * @param orderId Order ID
     * @return Number of times algorithmic matching was executed (0-2)
     */
    int getAlgorithmicMatchingCount(Long orderId);
    
    /**
     * Manually trigger algorithmic matching (for testing)
     * @param orderId Order ID to process
     * @return List of trades if matching occurred
     */
    List<Trade> triggerAlgorithmicMatching(Long orderId);
}