package com.stofina.app.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SimpleOrderBookSnapshot {
    
    // CHECKPOINT 5.2 - WebSocket Ready Snapshot
    private final String symbol;
    private final List<OrderLevel> bids;
    private final List<OrderLevel> asks;
    private final BigDecimal bestBid;
    private final BigDecimal bestAsk;
    private final BigDecimal spread;
    private final LocalDateTime lastUpdateTime;
    private final int totalBidQuantity;
    private final int totalAskQuantity;
    
    public boolean isEmpty() {
        return (bids == null || bids.isEmpty()) && 
               (asks == null || asks.isEmpty());
    }
    
    public boolean hasSpread() {
        return spread != null && spread.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public int getTotalOrderCount() {
        int bidCount = bids != null ? bids.stream().mapToInt(OrderLevel::getOrderCount).sum() : 0;
        int askCount = asks != null ? asks.stream().mapToInt(OrderLevel::getOrderCount).sum() : 0;
        return bidCount + askCount;
    }
}