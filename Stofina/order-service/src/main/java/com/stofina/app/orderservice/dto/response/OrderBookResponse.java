package com.stofina.app.orderservice.dto.response;

import com.stofina.app.orderservice.model.OrderLevel;
import com.stofina.app.orderservice.model.SimpleOrderBookSnapshot;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderBookResponse {
    
    // CHECKPOINT 5.3 - Order Book Response DTO
    private String symbol;
    private List<OrderLevel> bids;
    private List<OrderLevel> asks;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal spread;
    private LocalDateTime lastUpdateTime;
    private int totalBidQuantity;
    private int totalAskQuantity;
    private int totalOrderCount;
    private String status;
    
    public static OrderBookResponse fromSnapshot(
            SimpleOrderBookSnapshot snapshot) {
        
        if (snapshot == null) {
            return createEmptyResponse("UNKNOWN");
        }
        
        String status = snapshot.isEmpty() ? "EMPTY" : "ACTIVE";
        
        return new OrderBookResponse(
            snapshot.getSymbol(),
            snapshot.getBids(),
            snapshot.getAsks(),
            snapshot.getBestBid(),
            snapshot.getBestAsk(),
            snapshot.getSpread(),
            snapshot.getLastUpdateTime(),
            snapshot.getTotalBidQuantity(),
            snapshot.getTotalAskQuantity(),
            snapshot.getTotalOrderCount(),
            status
        );
    }
    
    public static OrderBookResponse createEmptyResponse(String symbol) {
        return new OrderBookResponse(
            symbol, null, null, null, null, null, 
            LocalDateTime.now(), 0, 0, 0, "EMPTY"
        );
    }
    
    public boolean isEmpty() {
        return totalOrderCount == 0;
    }
    
    public boolean hasSpread() {
        return spread != null && spread.compareTo(BigDecimal.ZERO) > 0;
    }
}