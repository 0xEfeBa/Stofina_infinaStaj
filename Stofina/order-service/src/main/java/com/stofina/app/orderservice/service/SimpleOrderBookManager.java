package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.model.SimpleOrderBook;
import com.stofina.app.orderservice.model.SimpleOrderBookSnapshot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface SimpleOrderBookManager {
    
    // CHECKPOINT 5.2 - Order Book Management Contract
    
    void initializeOrderBook(String symbol);
    
    List<Trade> addOrder(Order order);
    
    boolean removeOrder(Long orderId, String symbol);
    
    boolean updateOrder(Order oldOrder, Order newOrder);
    
    SimpleOrderBook getOrderBook(String symbol);
    
    SimpleOrderBookSnapshot getOrderBookSnapshot(String symbol);
    
    BigDecimal getBestBid(String symbol);
    
    BigDecimal getBestAsk(String symbol);
    
    BigDecimal getSpread(String symbol);
    
    void clearOrderBook(String symbol);
    
    Set<String> getActiveSymbols();
    
    int getTotalOrderCount(String symbol);
    
    boolean isSymbolActive(String symbol);
    
    // CHECKPOINT ENTEGRASYON 2.1 - Matching methods
    List<Trade> matchOrder(Order newOrder);
    
    List<Order> getMatchingOrders(Order newOrder);
}