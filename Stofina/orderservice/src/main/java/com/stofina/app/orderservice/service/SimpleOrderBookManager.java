package com.stofina.orderservice.service;

import com.stofina.orderservice.entity.Order;
import com.stofina.orderservice.model.SimpleOrderBook;
import com.stofina.orderservice.model.SimpleOrderBookSnapshot;

import java.math.BigDecimal;
import java.util.Set;

public interface SimpleOrderBookManager {
    
    // CHECKPOINT 5.2 - Order Book Management Contract
    
    void initializeOrderBook(String symbol);
    
    boolean addOrder(Order order);
    
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
}