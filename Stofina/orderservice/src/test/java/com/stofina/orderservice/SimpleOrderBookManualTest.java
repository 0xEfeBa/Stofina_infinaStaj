package com.stofina.orderservice;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderSide;
import com.stofina.orderservice.enums.OrderType;
import com.stofina.orderservice.model.OrderLevel;
import com.stofina.orderservice.model.SimpleOrderBook;

import java.math.BigDecimal;
import java.util.List;

public class SimpleOrderBookManualTest {

    public static void main(String[] args) {
        System.out.println("🧪 SimpleOrderBook Manual Test Started");
        
        // CHECKPOINT 5.1 - Basic Order Book Test
        testBasicOrderBookOperations();
        testBestBidAskCalculations();
        testOrderRemoval();
        testEdgeCases();
        
        System.out.println("✅ All tests completed successfully!");
    }
    
    private static void testBasicOrderBookOperations() {
        System.out.println("\n--- Test 1: Basic Operations ---");
        
        SimpleOrderBook orderBook = new SimpleOrderBook("THYAO");
        System.out.println("✅ Order book created for symbol: " + orderBook.getSymbol());
        
        // Test initial state
        assert orderBook.isEmpty() : "Order book should be empty initially";
        assert orderBook.getTotalOrderCount() == 0 : "Order count should be 0";
        System.out.println("✅ Initial state verified");
        
        // Add buy order
        Order buyOrder = createMockOrder(1L, OrderSide.BUY, new BigDecimal("45.50"), new BigDecimal("100"));
        orderBook.addOrder(buyOrder);
        
        assert !orderBook.isEmpty() : "Order book should not be empty after adding order";
        assert orderBook.getTotalOrderCount() == 1 : "Order count should be 1";
        System.out.println("✅ Buy order added successfully");
        
        // Add sell order
        Order sellOrder = createMockOrder(2L, OrderSide.SELL, new BigDecimal("46.00"), new BigDecimal("150"));
        orderBook.addOrder(sellOrder);
        
        assert orderBook.getTotalOrderCount() == 2 : "Order count should be 2";
        System.out.println("✅ Sell order added successfully");
    }
    
    private static void testBestBidAskCalculations() {
        System.out.println("\n--- Test 2: Best Bid/Ask Calculations ---");
        
        SimpleOrderBook orderBook = new SimpleOrderBook("THYAO");
        
        // Add multiple buy orders (bids)
        orderBook.addOrder(createMockOrder(1L, OrderSide.BUY, new BigDecimal("45.50"), new BigDecimal("100")));
        orderBook.addOrder(createMockOrder(2L, OrderSide.BUY, new BigDecimal("45.75"), new BigDecimal("200"))); // Best bid
        orderBook.addOrder(createMockOrder(3L, OrderSide.BUY, new BigDecimal("45.25"), new BigDecimal("150")));
        
        // Add multiple sell orders (asks)  
        orderBook.addOrder(createMockOrder(4L, OrderSide.SELL, new BigDecimal("46.25"), new BigDecimal("100")));
        orderBook.addOrder(createMockOrder(5L, OrderSide.SELL, new BigDecimal("46.00"), new BigDecimal("200"))); // Best ask
        orderBook.addOrder(createMockOrder(6L, OrderSide.SELL, new BigDecimal("46.50"), new BigDecimal("150")));
        
        BigDecimal bestBid = orderBook.getBestBid();
        BigDecimal bestAsk = orderBook.getBestAsk();
        BigDecimal spread = orderBook.getSpread();
        
        assert bestBid.compareTo(new BigDecimal("45.75")) == 0 : "Best bid should be 45.75";
        assert bestAsk.compareTo(new BigDecimal("46.00")) == 0 : "Best ask should be 46.00"; 
        assert spread.compareTo(new BigDecimal("0.25")) == 0 : "Spread should be 0.25";
        
        System.out.println("✅ Best Bid: " + bestBid);
        System.out.println("✅ Best Ask: " + bestAsk);
        System.out.println("✅ Spread: " + spread);
    }
    
    private static void testOrderRemoval() {
        System.out.println("\n--- Test 3: Order Removal ---");
        
        SimpleOrderBook orderBook = new SimpleOrderBook("THYAO");
        
        Order order1 = createMockOrder(1L, OrderSide.BUY, new BigDecimal("45.50"), new BigDecimal("100"));
        Order order2 = createMockOrder(2L, OrderSide.BUY, new BigDecimal("45.75"), new BigDecimal("200"));
        
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        
        assert orderBook.getTotalOrderCount() == 2 : "Should have 2 orders";
        
        boolean removed = orderBook.removeOrder(1L);
        assert removed : "Order removal should succeed";
        assert orderBook.getTotalOrderCount() == 1 : "Should have 1 order after removal";
        
        boolean removedAgain = orderBook.removeOrder(1L);
        assert !removedAgain : "Removing non-existent order should return false";
        
        System.out.println("✅ Order removal test passed");
    }
    
    private static void testEdgeCases() {
        System.out.println("\n--- Test 4: Edge Cases ---");
        
        // Test null symbol
        try {
            new SimpleOrderBook(null);
            assert false : "Should throw exception for null symbol";
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Null symbol validation works");
        }
        
        // Test empty symbol
        try {
            new SimpleOrderBook("  ");
            assert false : "Should throw exception for empty symbol";
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Empty symbol validation works");
        }
        
        // Test symbol mismatch
        SimpleOrderBook orderBook = new SimpleOrderBook("THYAO");
        Order wrongSymbolOrder = createMockOrder(1L, OrderSide.BUY, new BigDecimal("45.50"), new BigDecimal("100"));
        wrongSymbolOrder.setSymbol("AKBNK");
        
        try {
            orderBook.addOrder(wrongSymbolOrder);
            assert false : "Should throw exception for symbol mismatch";
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Symbol mismatch validation works");
        }
        
        // Test empty order book best prices
        SimpleOrderBook emptyBook = new SimpleOrderBook("TEST");
        assert emptyBook.getBestBid() == null : "Best bid should be null for empty book";
        assert emptyBook.getBestAsk() == null : "Best ask should be null for empty book";
        assert emptyBook.getSpread() == null : "Spread should be null for empty book";
        System.out.println("✅ Empty order book edge cases handled");
    }
    
    private static Order createMockOrder(Long orderId, OrderSide side, BigDecimal price, BigDecimal quantity) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setSymbol("THYAO");
        order.setSide(side);
        order.setOrderType(OrderType.LIMIT_BUY);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setFilledQuantity(BigDecimal.ZERO);
        order.setTenantId(1L);
        order.setAccountId(1001L);
        order.setIsBot(false);
        return order;
    }
}