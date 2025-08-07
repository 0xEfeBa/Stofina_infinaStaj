package com.stofina.app.orderservice.dto;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.enums.MatchingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResult {
    private boolean success;
    private String message;
    private MatchingStrategy strategy;
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private List<Trade> trades;
    private Order userOrder;
    private Order counterBotOrder;
    private boolean outOfRange;
    
    public static MatchingResult successful(MatchingStrategy strategy, BigDecimal filledQuantity, 
                                          BigDecimal remainingQuantity, List<Trade> trades, 
                                          Order userOrder, Order counterBotOrder) {
        return new MatchingResult(true, "Matching successful", strategy, filledQuantity, 
                                remainingQuantity, trades, userOrder, counterBotOrder, false);
    }
    
    public static MatchingResult failed(String message) {
        return new MatchingResult(false, message, null, BigDecimal.ZERO, BigDecimal.ZERO, 
                                null, null, null, false);
    }
    
    public static MatchingResult outOfRange(String message) {
        return new MatchingResult(false, message, null, BigDecimal.ZERO, BigDecimal.ZERO, 
                                null, null, null, true);
    }
    
    public int getTradeCount() {
        return trades != null ? trades.size() : 0;
    }
    
    public boolean hasMatched() {
        return success && filledQuantity != null && filledQuantity.compareTo(BigDecimal.ZERO) > 0;
    }
}
