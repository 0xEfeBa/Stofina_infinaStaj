package com.stofina.orderservice;

import com.stofina.orderservice.dto.MatchingResult;
import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.*;
import com.stofina.orderservice.service.impl.UserOrderMatchingEngineImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserOrderMatchingEngineTest {

    public static void main(String[] args) {
        System.out.println("🧪 UserOrderMatchingEngine Test Suite");
        
        UserOrderMatchingEngineImpl matchingEngine = new UserOrderMatchingEngineImpl();
        
        // Test 1: Range kontrolü
        testRangeValidation(matchingEngine);
        
        // Test 2: Strategy selection
        testStrategySelection(matchingEngine);
        
        // Test 3: User order processing
        testUserOrderProcessing(matchingEngine);
        
        System.out.println("✅ All UserOrderMatchingEngine tests completed!");
    }
    
    private static void testRangeValidation(UserOrderMatchingEngineImpl engine) {
        System.out.println("\n--- Test 1: Range Validation (%1 rule) ---");
        
        BigDecimal currentPrice = new BigDecimal("45.50");
        
        // Valid orders (within ±1%)
        Order validOrder1 = createTestOrder("THYAO", OrderSide.BUY, new BigDecimal("45.52"), new BigDecimal("100"));
        Order validOrder2 = createTestOrder("THYAO", OrderSide.SELL, new BigDecimal("45.48"), new BigDecimal("200"));
        Order validOrder3 = createTestOrder("THYAO", OrderSide.BUY, new BigDecimal("45.95"), new BigDecimal("150")); // +0.99%
        
        // Invalid orders (outside ±1%)
        Order invalidOrder1 = createTestOrder("THYAO", OrderSide.BUY, new BigDecimal("44.40"), new BigDecimal("100")); // -2.4%
        Order invalidOrder2 = createTestOrder("THYAO", OrderSide.SELL, new BigDecimal("46.80"), new BigDecimal("200")); // +2.9%
        
        assert engine.isWithinValidRange(validOrder1, currentPrice) : "45.52 should be valid (+0.04%)";
        assert engine.isWithinValidRange(validOrder2, currentPrice) : "45.48 should be valid (-0.04%)";
        assert engine.isWithinValidRange(validOrder3, currentPrice) : "45.95 should be valid (+0.99%)";
        
        assert !engine.isWithinValidRange(invalidOrder1, currentPrice) : "44.40 should be invalid (-2.4%)";
        assert !engine.isWithinValidRange(invalidOrder2, currentPrice) : "46.80 should be invalid (+2.9%)";
        
        System.out.println("✅ Range validation working correctly");
        
        // Valid range boundaries
        BigDecimal minValid = engine.calculateValidPriceRange(currentPrice, false); // 45.05
        BigDecimal maxValid = engine.calculateValidPriceRange(currentPrice, true);  // 45.95
        
        System.out.println("✅ Valid price range: " + minValid + " - " + maxValid);
    }
    
    private static void testStrategySelection(UserOrderMatchingEngineImpl engine) {
        System.out.println("\n--- Test 2: Strategy Selection (30%-40%-30%) ---");
        
        int fullFillCount = 0;
        int partialFillCount = 0;
        int noFillCount = 0;
        int totalTests = 1000;
        
        for (int i = 0; i < totalTests; i++) {
            MatchingStrategy strategy = engine.selectRandomStrategy();
            switch (strategy) {
                case FULL_FILL: fullFillCount++; break;
                case PARTIAL_FILL: partialFillCount++; break;
                case NO_FILL: noFillCount++; break;
            }
        }
        
        double fullFillPercent = (fullFillCount * 100.0) / totalTests;
        double partialFillPercent = (partialFillCount * 100.0) / totalTests;
        double noFillPercent = (noFillCount * 100.0) / totalTests;
        
        System.out.println("Strategy distribution over " + totalTests + " tests:");
        System.out.println("  FULL_FILL: " + fullFillCount + " (" + String.format("%.1f", fullFillPercent) + "%)");
        System.out.println("  PARTIAL_FILL: " + partialFillCount + " (" + String.format("%.1f", partialFillPercent) + "%)");
        System.out.println("  NO_FILL: " + noFillCount + " (" + String.format("%.1f", noFillPercent) + "%)");
        
        // Rough validation (within 10% tolerance)
        assert Math.abs(fullFillPercent - 30) < 10 : "FULL_FILL should be ~30%";
        assert Math.abs(partialFillPercent - 40) < 10 : "PARTIAL_FILL should be ~40%";
        assert Math.abs(noFillPercent - 30) < 10 : "NO_FILL should be ~30%";
        
        System.out.println("✅ Strategy selection distribution looks correct");
    }
    
    private static void testUserOrderProcessing(UserOrderMatchingEngineImpl engine) {
        System.out.println("\n--- Test 3: User Order Processing ---");
        
        BigDecimal currentPrice = new BigDecimal("45.50");
        
        // Test Case 1: Valid order
        Order validOrder = createTestOrder("THYAO", OrderSide.BUY, new BigDecimal("45.52"), new BigDecimal("500"));
        MatchingResult result1 = engine.processUserOrder(validOrder, currentPrice);
        
        assert result1 != null : "Result should not be null";
        assert result1.isSuccess() : "Valid order should succeed";
        assert result1.getStrategy() != null : "Strategy should be selected";
        
        System.out.println("✅ Valid order processed: " + result1.getStrategy() + 
                          ", filled: " + result1.getFilledQuantity());
        
        // Test Case 2: Invalid order (out of range)
        Order invalidOrder = createTestOrder("THYAO", OrderSide.BUY, new BigDecimal("44.00"), new BigDecimal("300"));
        MatchingResult result2 = engine.processUserOrder(invalidOrder, currentPrice);
        
        assert result2 != null : "Result should not be null";
        assert !result2.isSuccess() : "Invalid order should fail";
        assert result2.getReason().contains("outside") : "Reason should mention range";
        
        System.out.println("✅ Invalid order rejected: " + result2.getReason());
        
        // Test Case 3: Counter-bot order generation
        Order testOrder = createTestOrder("THYAO", OrderSide.BUY, new BigDecimal("45.52"), new BigDecimal("1000"));
        
        Order fullFillBot = engine.generateCounterBotOrder(testOrder, MatchingStrategy.FULL_FILL, currentPrice);
        assert fullFillBot.getSide() == OrderSide.SELL : "Counter-bot should be opposite side";
        assert fullFillBot.getPrice().equals(testOrder.getPrice()) : "FULL_FILL should match price exactly";
        assert fullFillBot.getQuantity().equals(testOrder.getQuantity()) : "FULL_FILL should match quantity exactly";
        assert fullFillBot.getIsBot() : "Should be marked as bot order";
        
        Order partialFillBot = engine.generateCounterBotOrder(testOrder, MatchingStrategy.PARTIAL_FILL, currentPrice);
        assert partialFillBot.getQuantity().compareTo(testOrder.getQuantity()) < 0 : "PARTIAL_FILL should be less quantity";
        
        Order noFillBot = engine.generateCounterBotOrder(testOrder, MatchingStrategy.NO_FILL, currentPrice);
        assert !noFillBot.getPrice().equals(testOrder.getPrice()) : "NO_FILL should have different price";
        
        System.out.println("✅ Counter-bot order generation working correctly");
        System.out.println("  FULL_FILL bot: " + fullFillBot.getSide() + " " + fullFillBot.getPrice() + " × " + fullFillBot.getQuantity());
        System.out.println("  PARTIAL_FILL bot: " + partialFillBot.getSide() + " " + partialFillBot.getPrice() + " × " + partialFillBot.getQuantity());
        System.out.println("  NO_FILL bot: " + noFillBot.getSide() + " " + noFillBot.getPrice() + " × " + noFillBot.getQuantity());
    }
    
    private static Order createTestOrder(String symbol, OrderSide side, BigDecimal price, BigDecimal quantity) {
        Order order = new Order();
        order.setOrderId(System.currentTimeMillis());
        order.setTenantId(1L);
        order.setAccountId(1001L);
        order.setSymbol(symbol);
        order.setOrderType(OrderType.LIMIT_BUY);
        order.setSide(side);
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setFilledQuantity(BigDecimal.ZERO);
        order.setStatus(OrderStatus.NEW);
        order.setTimeInForce(TimeInForce.DAY);
        order.setIsBot(false);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
}