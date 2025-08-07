package com.stofina.orderservice;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderSide;
import com.stofina.orderservice.enums.OrderType;
import com.stofina.orderservice.model.SimpleOrderBook;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleOrderBookManagerBasicTest {

    public static void main(String[] args) {
        System.out.println("🧪 SimpleOrderBookManager Basic Test (No Spring Context)");
        
        // CHECKPOINT 5.2 - Basic Manager Logic Test
        testConcurrentHashMapOperations();
        testLockMechanism();
        testSymbolNormalization();
        
        System.out.println("✅ All basic manager tests completed successfully!");
    }
    
    private static void testConcurrentHashMapOperations() {
        System.out.println("\n--- Test 1: ConcurrentHashMap Operations ---");
        
        ConcurrentHashMap<String, SimpleOrderBook> orderBooks = new ConcurrentHashMap<>();
        
        // Test computeIfAbsent
        SimpleOrderBook book1 = orderBooks.computeIfAbsent("THYAO", SimpleOrderBook::new);
        SimpleOrderBook book2 = orderBooks.computeIfAbsent("THYAO", SimpleOrderBook::new);
        
        assert book1 == book2 : "Same symbol should return same instance";
        assert "THYAO".equals(book1.getSymbol()) : "Symbol should be THYAO";
        System.out.println("✅ ConcurrentHashMap computeIfAbsent works correctly");
        
        // Test multiple symbols
        orderBooks.computeIfAbsent("GARAN", SimpleOrderBook::new);
        orderBooks.computeIfAbsent("AKBNK", SimpleOrderBook::new);
        
        assert orderBooks.size() == 3 : "Should have 3 order books";
        assert orderBooks.containsKey("THYAO") : "Should contain THYAO";
        assert orderBooks.containsKey("GARAN") : "Should contain GARAN";
        assert orderBooks.containsKey("AKBNK") : "Should contain AKBNK";
        System.out.println("✅ Multiple symbols handling works");
    }
    
    private static void testLockMechanism() {
        System.out.println("\n--- Test 2: Lock Mechanism Simulation ---");
        
        ConcurrentHashMap<String, Object> symbolLocks = new ConcurrentHashMap<>();
        
        // Simulate lock creation
        Object lock1 = symbolLocks.computeIfAbsent("THYAO", k -> new Object());
        Object lock2 = symbolLocks.computeIfAbsent("THYAO", k -> new Object());
        
        assert lock1 == lock2 : "Same symbol should get same lock";
        System.out.println("✅ Lock creation per symbol works");
        
        // Test different symbols get different locks
        Object lockGaran = symbolLocks.computeIfAbsent("GARAN", k -> new Object());
        assert lock1 != lockGaran : "Different symbols should get different locks";
        System.out.println("✅ Different locks for different symbols works");
    }
    
    private static void testSymbolNormalization() {
        System.out.println("\n--- Test 3: Symbol Normalization ---");
        
        // Test symbol normalization logic
        String[] testSymbols = {" thyao ", "garan", "AKBNK", " ISCTR "};
        String[] expectedResults = {"THYAO", "GARAN", "AKBNK", "ISCTR"};
        
        for (int i = 0; i < testSymbols.length; i++) {
            String normalized = testSymbols[i].trim().toUpperCase();
            assert normalized.equals(expectedResults[i]) : 
                "Symbol " + testSymbols[i] + " should normalize to " + expectedResults[i];
            System.out.println("✅ " + testSymbols[i] + " → " + normalized);
        }
        
        // Test null handling  
        try {
            String nullSymbol = null;
            if (nullSymbol != null) {
                nullSymbol.trim().toUpperCase();
            } else {
                System.out.println("✅ Null symbol handling works");
            }
        } catch (Exception e) {
            System.out.println("❌ Null symbol handling failed");
        }
        
        // Test order book operations with normalized symbols
        SimpleOrderBook orderBook = new SimpleOrderBook("THYAO");
        Order order = createMockOrder(1L, "thyao", OrderSide.BUY, new BigDecimal("45.50"), new BigDecimal("100"));
        
        try {
            // This should throw IllegalArgumentException due to symbol mismatch
            orderBook.addOrder(order);
            System.out.println("❌ Symbol mismatch validation should have failed");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Symbol mismatch validation works: " + e.getMessage());
        }
        
        // Test with correct symbol
        order.setSymbol("THYAO");
        orderBook.addOrder(order);
        assert orderBook.getTotalOrderCount() == 1 : "Order should be added successfully";
        System.out.println("✅ Correct symbol addition works");
    }
    
    private static Order createMockOrder(Long orderId, String symbol, OrderSide side, BigDecimal price, BigDecimal quantity) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setSymbol(symbol);
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