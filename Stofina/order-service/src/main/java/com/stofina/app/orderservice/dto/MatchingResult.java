package com.stofina.app.orderservice.dto;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.enums.MatchingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResult {
    
    // CHECKPOINT 4.2 - User Order Matching Result
    private MatchingStrategy strategy;
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private List<Trade> trades;
    private Order updatedUserOrder;
    private Order counterBotOrder;
    private boolean success;
    private String reason;
    private LocalDateTime executionTime;
    
    public static MatchingResult outOfRange(String reason) {
        MatchingResult result = new MatchingResult();
        result.setStrategy(MatchingStrategy.NO_FILL);
        result.setFilledQuantity(BigDecimal.ZERO);
        result.setRemainingQuantity(BigDecimal.ZERO);
        result.setTrades(new ArrayList<>());
        result.setSuccess(false);
        result.setReason(reason);
        result.setExecutionTime(LocalDateTime.now());
        return result;
    }
    
    public static MatchingResult successful(MatchingStrategy strategy, 
                                          BigDecimal filledQuantity, 
                                          BigDecimal remainingQuantity,
                                          List<Trade> trades,
                                          Order updatedUserOrder,
                                          Order counterBotOrder) {
        MatchingResult result = new MatchingResult();
        result.setStrategy(strategy);
        result.setFilledQuantity(filledQuantity);
        result.setRemainingQuantity(remainingQuantity);
        result.setTrades(trades != null ? trades : new ArrayList<>());
        result.setUpdatedUserOrder(updatedUserOrder);
        result.setCounterBotOrder(counterBotOrder);
        result.setSuccess(true);
        result.setReason("Successfully processed");
        result.setExecutionTime(LocalDateTime.now());
        return result;
    }
    
    public static MatchingResult failed(String reason) {
        MatchingResult result = new MatchingResult();
        result.setStrategy(MatchingStrategy.NO_FILL);
        result.setFilledQuantity(BigDecimal.ZERO);
        result.setRemainingQuantity(BigDecimal.ZERO);
        result.setTrades(new ArrayList<>());
        result.setSuccess(false);
        result.setReason(reason);
        result.setExecutionTime(LocalDateTime.now());
        return result;
    }
    
    public boolean hasMatched() {
        return success && filledQuantity != null && filledQuantity.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isCompletelyFilled() {
        return hasMatched() && (remainingQuantity == null || remainingQuantity.compareTo(BigDecimal.ZERO) == 0);
    }
    
    public boolean isPartiallyFilled() {
        return hasMatched() && remainingQuantity != null && remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public int getTradeCount() {
        return trades != null ? trades.size() : 0;
    }
}