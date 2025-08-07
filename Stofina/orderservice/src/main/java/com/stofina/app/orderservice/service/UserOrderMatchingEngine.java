package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.dto.MatchingResult;
import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.MatchingStrategy;

import java.math.BigDecimal;

public interface UserOrderMatchingEngine {
    
    // CHECKPOINT 4.2 - User Order Matching Engine Contract
    
    boolean isWithinValidRange(Order userOrder, BigDecimal currentPrice);
    
    MatchingStrategy selectRandomStrategy();
    
    MatchingResult processUserOrder(Order userOrder, BigDecimal currentPrice);
    
    Order generateCounterBotOrder(Order userOrder, MatchingStrategy strategy, BigDecimal currentPrice);
    
    MatchingResult executeMatching(Order userOrder, Order counterBotOrder, MatchingStrategy strategy);
    
    BigDecimal getCurrentPrice(String symbol);
    
    BigDecimal calculateValidPriceRange(BigDecimal currentPrice, boolean isUpperBound);
}