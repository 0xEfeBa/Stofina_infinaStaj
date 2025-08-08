package com.stofina.app.orderservice.model;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.OrderSide;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class SimpleOrderBook {
    
    // CHECKPOINT 5.1 - Core Data Structure
    private static final int TOP_LEVELS = 10;
    
    private final TreeMap<BigDecimal, Queue<Order>> bids;
    private final TreeMap<BigDecimal, Queue<Order>> asks;
    private final String symbol;
    private LocalDateTime lastUpdateTime;
    
    public SimpleOrderBook(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        this.symbol = symbol.trim().toUpperCase();
        this.bids = new TreeMap<>(Collections.reverseOrder()); // Descending
        this.asks = new TreeMap<>(); // Ascending
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public void addOrder(Order order) {
        if (order == null) {
            return;
        }
        
        if (!symbol.equals(order.getSymbol())) {
            throw new IllegalArgumentException("Order symbol mismatch");
        }
        
        TreeMap<BigDecimal, Queue<Order>> targetSide = getTargetSide(order.getSide());
        BigDecimal price = getOrderPrice(order);
        
        targetSide.computeIfAbsent(price, k -> new LinkedBlockingQueue<>()).offer(order);
        updateTimestamp();
    }
    
    public boolean removeOrder(Long orderId) {
        if (orderId == null) {
            return false;
        }
        
        boolean removed = removeOrderFromSide(bids, orderId) || 
                         removeOrderFromSide(asks, orderId);
        
        if (removed) {
            updateTimestamp();
        }
        
        return removed;
    }
    
    public BigDecimal getBestBid() {
        if (bids.isEmpty()) {
            return null;
        }
        return bids.firstKey();
    }
    
    public BigDecimal getBestAsk() {
        if (asks.isEmpty()) {
            return null;
        }
        return asks.firstKey();
    }
    
    public BigDecimal getSpread() {
        BigDecimal bestBid = getBestBid();
        BigDecimal bestAsk = getBestAsk();
        
        if (bestBid == null || bestAsk == null) {
            return null;
        }
        
        return bestAsk.subtract(bestBid);
    }
    
    public boolean isEmpty() {
        return bids.isEmpty() && asks.isEmpty();
    }
    
    public int getTotalOrderCount() {
        return bids.values().stream().mapToInt(Queue::size).sum() +
               asks.values().stream().mapToInt(Queue::size).sum();
    }
    
    public List<OrderLevel> getTop10Bids() {
        return getTopLevels(bids, TOP_LEVELS);
    }
    
    public List<OrderLevel> getTop10Asks() {
        return getTopLevels(asks, TOP_LEVELS);
    }
    
    // CHECKPOINT ENTEGRASYON 2.5 - Get orders for matching
    public List<Order> getBidOrders() {
        List<Order> bidOrders = new ArrayList<>();
        for (Queue<Order> orders : bids.values()) {
            bidOrders.addAll(orders);
        }
        return bidOrders;
    }
    
    public List<Order> getAskOrders() {
        List<Order> askOrders = new ArrayList<>();
        for (Queue<Order> orders : asks.values()) {
            askOrders.addAll(orders);
        }
        return askOrders;
    }
    
    private TreeMap<BigDecimal, Queue<Order>> getTargetSide(OrderSide side) {
        return side == OrderSide.BUY ? bids : asks;
    }
    
    private BigDecimal getOrderPrice(Order order) {
        return order.getPrice() != null ? order.getPrice() : BigDecimal.ZERO;
    }
    
    private boolean removeOrderFromSide(TreeMap<BigDecimal, Queue<Order>> side, Long orderId) {
        for (Iterator<Map.Entry<BigDecimal, Queue<Order>>> iterator = side.entrySet().iterator(); 
             iterator.hasNext();) {
            
            Map.Entry<BigDecimal, Queue<Order>> entry = iterator.next();
            Queue<Order> orders = entry.getValue();
            
            if (orders.removeIf(order -> orderId.equals(order.getOrderId()))) {
                if (orders.isEmpty()) {
                    iterator.remove();
                }
                return true;
            }
        }
        return false;
    }
    
    private List<OrderLevel> getTopLevels(TreeMap<BigDecimal, Queue<Order>> side, int limit) {
        List<OrderLevel> levels = new ArrayList<>();
        int count = 0;
        
        for (Map.Entry<BigDecimal, Queue<Order>> entry : side.entrySet()) {
            if (count >= limit) {
                break;
            }
            
            BigDecimal price = entry.getKey();
            Queue<Order> orders = entry.getValue();
            BigDecimal totalQuantity = calculateTotalQuantity(orders);
            
            levels.add(new OrderLevel(price, totalQuantity, orders.size()));
            count++;
        }
        
        return levels;
    }
    
    private BigDecimal calculateTotalQuantity(Queue<Order> orders) {
        return orders.stream()
                    .map(Order::getRemainingQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void updateTimestamp() {
        this.lastUpdateTime = LocalDateTime.now();
    }
}