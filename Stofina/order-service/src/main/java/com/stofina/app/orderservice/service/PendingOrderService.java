package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.entity.Order;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing orders that are pending activation due to price range validation.
 * Orders are held when their price is outside the ±1.5% range from current market price.
 * When Kafka price updates bring the order within range, it gets activated automatically.
 */
public interface PendingOrderService {
    
    /**
     * Add an order to pending activation queue
     * @param order Order to be held for activation
     * @param currentMarketPrice Current market price when order was validated
     * @param submittedPrice Price submitted by user
     */
    void addToPendingActivation(Order order, BigDecimal currentMarketPrice, BigDecimal submittedPrice);
    
    /**
     * Check all pending orders for a symbol and activate eligible ones
     * @param symbol Stock symbol
     * @param newMarketPrice New market price from Kafka
     * @return List of orders that were activated
     */
    List<Order> checkAndActivatePendingOrders(String symbol, BigDecimal newMarketPrice);
    
    /**
     * Remove order from pending activation (used when order is cancelled)
     * @param orderId Order ID to remove
     * @return true if order was found and removed
     */
    boolean removePendingOrder(Long orderId);
    
    /**
     * Get all pending orders for a symbol
     * @param symbol Stock symbol
     * @return List of pending orders
     */
    List<Order> getPendingOrdersBySymbol(String symbol);
    
    /**
     * Get all pending orders
     * @return List of all pending orders
     */
    List<Order> getAllPendingOrders();
    
    /**
     * Check if price is within acceptable range (±1.5%)
     * @param currentPrice Current market price
     * @param orderPrice Order price
     * @return true if within range
     */
    boolean isPriceWithinRange(BigDecimal currentPrice, BigDecimal orderPrice);
}