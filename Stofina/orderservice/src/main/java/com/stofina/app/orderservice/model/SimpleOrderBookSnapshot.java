package com.stofina.app.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleOrderBookSnapshot {
    private String symbol;
    private List<OrderLevel> bids;
    private List<OrderLevel> asks;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal spread;
    private LocalDateTime timestamp;
    private int totalBidQuantity;
    private int totalAskQuantity;
    
    public boolean isEmpty() {
        return (bids == null || bids.isEmpty()) && (asks == null || asks.isEmpty());
    }
    
    public boolean hasSpread() {
        return bestBid != null && bestAsk != null;
    }
    
    public int getTotalOrderCount() {
        int bidCount = bids != null ? bids.size() : 0;
        int askCount = asks != null ? asks.size() : 0;
        return bidCount + askCount;
    }
}
