package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.model.DisplayOrder;
import com.stofina.app.orderservice.model.SimpleOrderBookSnapshot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface DisplayOrderBookService {
    
    // CHECKPOINT 4.1 - Display-Only Order Book Contract
    
    void initializeDisplayOrderBook(String symbol, BigDecimal currentPrice);
    
    void updateDisplayPrices(String symbol, BigDecimal newPrice);
    
    SimpleOrderBookSnapshot getDisplaySnapshot(String symbol);
    
    void addUserOrderToDisplay(Order userOrder);
    
    void removeFromDisplay(String symbol, Long orderId);
    
    Set<String> getActiveDisplaySymbols();
    
    int getDisplayOrderCount(String symbol);
    
    BigDecimal getDisplayBestBid(String symbol);
    
    BigDecimal getDisplayBestAsk(String symbol);
    
    void clearDisplayOrderBook(String symbol);
    
    void maintainDisplayDepth(String symbol, BigDecimal currentPrice);
    
    List<DisplayOrder> getUserOrdersForSymbol(String symbol);
}