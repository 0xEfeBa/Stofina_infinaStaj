package com.stofina.app.orderservice.service.impl;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.repository.OrderRepository;
import com.stofina.app.orderservice.service.PendingOrderService;
import com.stofina.app.orderservice.service.AlgorithmicMatchingService;
import com.stofina.app.orderservice.service.SimpleOrderBookManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingOrderServiceImpl implements PendingOrderService {

    private final OrderRepository orderRepository;
    private final SimpleOrderBookManager orderBookManager;
    private final AlgorithmicMatchingService algorithmicMatchingService;
    
    // Thread-safe in-memory cache for fast access
    private final List<Order> pendingOrders = new CopyOnWriteArrayList<>();
    
    // 1.5% price deviation limit
    private static final BigDecimal PRICE_DEVIATION_LIMIT = new BigDecimal("0.015");

    @Override
    @Transactional
    public void addToPendingActivation(Order order, BigDecimal currentMarketPrice, BigDecimal submittedPrice) {
        log.info("🟡 PENDING: Adding order to pending activation - OrderId: {}, Symbol: {}, " +
                "SubmittedPrice: {}, MarketPrice: {}", 
                order.getOrderId(), order.getSymbol(), submittedPrice, currentMarketPrice);
        
        // Set order status to PENDING_TRIGGER
        order.setStatus(OrderStatus.PENDING_TRIGGER);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Save to database
        orderRepository.save(order);
        
        // Add to in-memory cache for fast access
        pendingOrders.add(order);
        
        log.info("✅ PENDING: Order added to pending queue - OrderId: {}, Current pending count: {}", 
                order.getOrderId(), pendingOrders.size());
    }

    @Override
    @Transactional
    public List<Order> checkAndActivatePendingOrders(String symbol, BigDecimal newMarketPrice) {
        log.debug("🔍 PENDING: Checking pending orders for symbol: {}, New price: {}, Pending count: {}", 
                symbol, newMarketPrice, pendingOrders.size());
        
        List<Order> activatedOrders = new CopyOnWriteArrayList<>();
        
        // Iterator to safely remove items while iterating
        var iterator = pendingOrders.iterator();
        while (iterator.hasNext()) {
            Order pendingOrder = iterator.next();
            
            // Check if this order matches the symbol and is eligible for activation
            if (symbol.equals(pendingOrder.getSymbol()) && 
                isPriceWithinRange(newMarketPrice, pendingOrder.getPrice())) {
                
                log.info("🟢 PENDING: Activating order - OrderId: {}, Symbol: {}, " +
                        "OrderPrice: {}, NewMarketPrice: {}", 
                        pendingOrder.getOrderId(), symbol, pendingOrder.getPrice(), newMarketPrice);
                
                // Activate the order
                Order activatedOrder = activateOrder(pendingOrder);
                if (activatedOrder != null) {
                    activatedOrders.add(activatedOrder);
                    iterator.remove(); // Remove from pending list
                    
                    // Add to order book and trigger matching
                    orderBookManager.addOrder(activatedOrder);
                    
                    log.info("✅ PENDING: Order activated and added to order book - OrderId: {}", 
                            activatedOrder.getOrderId());
                }
            }
        }
        
        if (!activatedOrders.isEmpty()) {
            log.info("🟢 PENDING: Activated {} orders for symbol: {} at price: {}", 
                    activatedOrders.size(), symbol, newMarketPrice);
        }
        
        return activatedOrders;
    }

    @Override
    @Transactional
    public boolean removePendingOrder(Long orderId) {
        log.info("🗑️ PENDING: Removing pending order - OrderId: {}", orderId);
        
        boolean removed = pendingOrders.removeIf(order -> order.getOrderId().equals(orderId));
        
        if (removed) {
            // Also update database status to CANCELLED
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            });
            
            log.info("✅ PENDING: Pending order removed - OrderId: {}", orderId);
        } else {
            log.warn("⚠️ PENDING: Order not found in pending list - OrderId: {}", orderId);
        }
        
        return removed;
    }

    @Override
    public List<Order> getPendingOrdersBySymbol(String symbol) {
        return pendingOrders.stream()
                .filter(order -> symbol.equals(order.getSymbol()))
                .toList();
    }

    @Override
    public List<Order> getAllPendingOrders() {
        return List.copyOf(pendingOrders);
    }

    @Override
    public boolean isPriceWithinRange(BigDecimal currentPrice, BigDecimal orderPrice) {
        if (currentPrice == null || orderPrice == null || 
            currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        BigDecimal deviation = orderPrice.subtract(currentPrice).abs()
                .divide(currentPrice, 6, BigDecimal.ROUND_HALF_UP);
        boolean withinRange = deviation.compareTo(PRICE_DEVIATION_LIMIT) <= 0;
        
        log.debug("🔍 PENDING: Price range check - CurrentPrice: {}, OrderPrice: {}, " +
                "Deviation: {}%, Limit: {}%, WithinRange: {}", 
                currentPrice, orderPrice, deviation.multiply(new BigDecimal("100")), 
                PRICE_DEVIATION_LIMIT.multiply(new BigDecimal("100")), withinRange);
        
        return withinRange;
    }
    
    /**
     * Activate a pending order by changing its status and updating database
     */
    @Transactional
    private Order activateOrder(Order pendingOrder) {
        try {
            // Change status to ACTIVE
            pendingOrder.setStatus(OrderStatus.ACTIVE);
            pendingOrder.setUpdatedAt(LocalDateTime.now());
            
            // Save to database
            Order savedOrder = orderRepository.save(pendingOrder);
            
            log.info("🟢 PENDING: Order status changed to ACTIVE - OrderId: {}", savedOrder.getOrderId());
            return savedOrder;
            
        } catch (Exception e) {
            log.error("❌ PENDING: Failed to activate order - OrderId: {}", pendingOrder.getOrderId(), e);
            return null;
        }
    }
}