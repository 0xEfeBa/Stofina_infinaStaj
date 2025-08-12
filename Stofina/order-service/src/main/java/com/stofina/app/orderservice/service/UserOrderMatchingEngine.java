package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.MatchingResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.MatchingStrategy;

import java.math.BigDecimal;

/**
 * Service interface for matching user orders with counter-party orders.
 * Implements order matching logic and price validation.
 */
public interface UserOrderMatchingEngine {
    
    /**
     * Checks if a user order's price is within valid range of current market price.
     *
     * @param userOrder the order to validate
     * @param currentPrice the current market price
     * @return true if price is within valid range, false otherwise
     */
    boolean isWithinValidRange(Order userOrder, BigDecimal currentPrice);
    
    /**
     * Randomly selects a matching strategy for order processing.
     *
     * @return the selected matching strategy
     */
    MatchingStrategy selectRandomStrategy();
    
    /**
     * Processes a user order for potential matching.
     *
     * @param userOrder the order to process
     * @param currentPrice the current market price
     * @return result of the matching process
     */
    MatchingResult processUserOrder(Order userOrder, BigDecimal currentPrice);
    
    /**
     * Generates a counter-party bot order to match against user order.
     *
     * @param userOrder the user's order to match against
     * @param strategy the matching strategy to use
     * @param currentPrice the current market price
     * @return generated counter-party order
     */
    Order generateCounterBotOrder(Order userOrder, MatchingStrategy strategy, BigDecimal currentPrice);
    
    /**
     * Executes matching between user order and counter-party order.
     *
     * @param userOrder the user's order
     * @param counterBotOrder the counter-party order
     * @param strategy the matching strategy to use
     * @return result of the matching execution
     */
    MatchingResult executeMatching(Order userOrder, Order counterBotOrder, MatchingStrategy strategy);
    
    /**
     * Gets the current market price for a symbol.
     *
     * @param symbol the stock symbol
     * @return current market price
     */
    BigDecimal getCurrentPrice(String symbol);
    
    /**
     * Calculates valid price range for order matching.
     *
     * @param currentPrice the current market price
     * @param isUpperBound true to calculate upper bound, false for lower bound
     * @return calculated price bound
     */
    BigDecimal calculateValidPriceRange(BigDecimal currentPrice, boolean isUpperBound);
}