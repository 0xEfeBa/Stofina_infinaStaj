package com.stofina.orderservice;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.enums.OrderSide;
import com.stofina.orderservice.enums.OrderType;
import com.stofina.orderservice.model.SimpleOrderBookSnapshot;
import com.stofina.orderservice.service.impl.SimpleOrderBookManagerImpl;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleOrderBookManagerTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🧪 SimpleOrderBookManager Test Started");
        
        // CHECKPOINT 5.2 - Manager Tests
        testBasicManagerOperations();
        testMockSymbolsInitialization();
        testThreadSafetySimulation();
        testSnapshotGeneration();
        
        System.out.println("✅ All manager tests completed successfully!");
    }
    
    private static void testBasicManagerOperations() {
        System.out.println("\n--- Test 1: Basic Manager Operations ---");
        
        SimpleOrderBookManagerImpl manager = new SimpleOrderBookManagerImpl();
        manager.initializeMockSymbols();
        
        // Test active symbols
        Set<String> activeSymbols = manager.getActiveSymbols();
        assert activeSymbols.size() == 10 : "Should have 10 mock symbols";
        System.out.println("✅ Active symbols count: " + activeSymbols.size());
        System.out.println("✅ Mock symbols: " + activeSymbols);
        
        // Test individual symbol operations
        assert manager.isSymbolActive("THYAO") : "THYAO should be active";
        assert manager.isSymbolActive("GARAN") : "GARAN should be active";
        assert !manager.isSymbolActive("INVALID") : "INVALID should not be active";
        System.out.println("✅ Symbol activity checks passed");
        
        // Test order operations
        Order buyOrder = createMockOrder(1L, "THYAO", OrderSide.BUY, new BigDecimal("45.50"), new BigDecimal("100"));
        boolean added = manager.addOrder(buyOrder);
        assert added : "Order addition should succeed";
        System.out.println("✅ Order addition successful");
        
        // Test best bid/ask
        BigDecimal bestBid = manager.getBestBid("THYAO");
        assert bestBid != null : "Best bid should not be null";
        assert bestBid.compareTo(new BigDecimal("45.50")) == 0 : "Best bid should be 45.50";
        System.out.println("✅ Best bid: " + bestBid);
        
        // Test order count
        int orderCount = manager.getTotalOrderCount("THYAO");
        assert orderCount == 1 : "Order count should be 1";
        System.out.println("✅ Order count: " + orderCount);
    }
    
    private static void testMockSymbolsInitialization() {
        System.out.println("\n--- Test 2: Mock Symbols Initialization ---");
        
        SimpleOrderBookManagerImpl manager = new SimpleOrderBookManagerImpl();
        manager.initializeMockSymbols();
        
        String[] expectedSymbols = {"THYAO", "GARAN", "AKBNK", "ISCTR", "TUPRS", 
                                  "ASELS", "SISE", "BIMAS", "KCHOL", "TCELL"};
        
        for (String symbol : expectedSymbols) {
            assert manager.isSymbolActive(symbol) : symbol + " should be active";
            assert manager.getTotalOrderCount(symbol) == 0 : symbol + " should have 0 orders initially";
            System.out.println("✅ " + symbol + " initialized correctly");
        }
        
        System.out.println("✅ All mock symbols initialized correctly");
    }
    
    private static void testThreadSafetySimulation() throws InterruptedException {
        System.out.println("\n--- Test 3: Thread Safety Simulation ---");
        
        SimpleOrderBookManagerImpl manager = new SimpleOrderBookManagerImpl();
        manager.initializeMockSymbols();
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(20);
        
        // Simulate concurrent order additions
        for (int i = 0; i < 20; i++) {
            final int orderId = i;
            executor.submit(() -> {
                try {
                    Order order = createMockOrder(
                        (long) orderId, 
                        "THYAO", 
                        orderId % 2 == 0 ? OrderSide.BUY : OrderSide.SELL,
                        new BigDecimal("45." + (50 + orderId)),
                        new BigDecimal("100")
                    );
                    manager.addOrder(order);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        int finalOrderCount = manager.getTotalOrderCount("THYAO");
        assert finalOrderCount == 20 : "Should have 20 orders after concurrent additions";
        System.out.println("✅ Thread safety test passed - Final order count: " + finalOrderCount);
    }
    
    private static void testSnapshotGeneration() {
        System.out.println("\n--- Test 4: Snapshot Generation ---");
        
        SimpleOrderBookManagerImpl manager = new SimpleOrderBookManagerImpl();
        manager.initializeMockSymbols();
        
        // Add some orders
        manager.addOrder(createMockOrder(1L, "THYAO", OrderSide.BUY, new BigDecimal("45.50"), new BigDecimal("100")));
        manager.addOrder(createMockOrder(2L, "THYAO", OrderSide.SELL, new BigDecimal("46.00"), new BigDecimal("150")));
        
        // Test snapshot generation
        SimpleOrderBookSnapshot snapshot = manager.getOrderBookSnapshot("THYAO");
        assert snapshot != null : "Snapshot should not be null";
        assert "THYAO".equals(snapshot.getSymbol()) : "Snapshot symbol should be THYAO";
        assert !snapshot.isEmpty() : "Snapshot should not be empty";
        assert snapshot.getBestBid() != null : "Snapshot should have best bid";
        assert snapshot.getBestAsk() != null : "Snapshot should have best ask";
        assert snapshot.hasSpread() : "Snapshot should have spread";
        
        System.out.println("✅ Snapshot generated: " + snapshot.getSymbol());
        System.out.println("✅ Best Bid: " + snapshot.getBestBid());
        System.out.println("✅ Best Ask: " + snapshot.getBestAsk());
        System.out.println("✅ Spread: " + snapshot.getSpread());
        System.out.println("✅ Total Orders: " + snapshot.getTotalOrderCount());
        
        // Test empty snapshot
        SimpleOrderBookSnapshot emptySnapshot = manager.getOrderBookSnapshot("GARAN");
        assert emptySnapshot != null : "Empty snapshot should not be null";
        assert emptySnapshot.isEmpty() : "Empty snapshot should be empty";
        System.out.println("✅ Empty snapshot handling works");
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