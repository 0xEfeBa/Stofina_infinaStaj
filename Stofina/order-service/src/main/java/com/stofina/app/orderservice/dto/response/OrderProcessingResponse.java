package com.stofina.app.orderservice.dto.response;

import com.stofina.app.orderservice.dto.MatchingResult;
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
public class OrderProcessingResponse {
    
    // CHECKPOINT 5.0 - User Order Processing Response
    private Long orderId;
    private String symbol;
    private MatchingStrategy strategy;
    private BigDecimal originalQuantity;
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal averagePrice;
    private List<TradeInfo> trades;
    private boolean hasMatched;
    private boolean isCompletelyFilled;
    private boolean isPartiallyFilled;
    private String status;
    private String message;
    private LocalDateTime processedAt;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradeInfo {
        private Long tradeId;
        private BigDecimal price;
        private BigDecimal quantity;
        private LocalDateTime executedAt;
        private String counterParty; // "BOT" for counter-bot trades
    }
    
    public static OrderProcessingResponse withTrades(MatchingResult result, Order userOrder) {
        if (result == null || userOrder == null) {
            return failed("Invalid matching result or user order");
        }
        
        OrderProcessingResponse response = new OrderProcessingResponse();
        response.setOrderId(userOrder.getOrderId());
        response.setSymbol(userOrder.getSymbol());
        response.setStrategy(result.getStrategy());
        response.setOriginalQuantity(userOrder.getQuantity());
        response.setFilledQuantity(result.getFilledQuantity());
        response.setRemainingQuantity(result.getRemainingQuantity());
        response.setAveragePrice(userOrder.getAveragePrice());
        response.setTrades(convertTradesToInfo(result.getTrades()));
        response.setHasMatched(result.hasMatched());
        response.setCompletelyFilled(result.isCompletelyFilled());
        response.setPartiallyFilled(result.isPartiallyFilled());
        response.setProcessedAt(LocalDateTime.now());
        
        if (result.isCompletelyFilled()) {
            response.setStatus("FILLED");
            response.setMessage("Order completely filled");
        } else if (result.isPartiallyFilled()) {
            response.setStatus("PARTIALLY_FILLED");
            response.setMessage("Order partially filled, remaining quantity added to order book");
        } else {
            response.setStatus("ADDED_TO_BOOK");
            response.setMessage("Order added to order book without immediate matching");
        }
        
        return response;
    }
    
    public static OrderProcessingResponse noMatch(Order userOrder, String reason) {
        if (userOrder == null) {
            return failed("User order is null");
        }
        
        OrderProcessingResponse response = new OrderProcessingResponse();
        response.setOrderId(userOrder.getOrderId());
        response.setSymbol(userOrder.getSymbol());
        response.setStrategy(MatchingStrategy.NO_FILL);
        response.setOriginalQuantity(userOrder.getQuantity());
        response.setFilledQuantity(BigDecimal.ZERO);
        response.setRemainingQuantity(userOrder.getQuantity());
        response.setAveragePrice(null);
        response.setTrades(new ArrayList<>());
        response.setHasMatched(false);
        response.setCompletelyFilled(false);
        response.setPartiallyFilled(false);
        response.setStatus("ADDED_TO_BOOK");
        response.setMessage(reason != null ? reason : "Order added to order book");
        response.setProcessedAt(LocalDateTime.now());
        
        return response;
    }
    
    public static OrderProcessingResponse outOfRange(Order userOrder, String reason) {
        if (userOrder == null) {
            return failed("User order is null");
        }
        
        OrderProcessingResponse response = new OrderProcessingResponse();
        response.setOrderId(userOrder.getOrderId());
        response.setSymbol(userOrder.getSymbol());
        response.setStrategy(null);
        response.setOriginalQuantity(userOrder.getQuantity());
        response.setFilledQuantity(BigDecimal.ZERO);
        response.setRemainingQuantity(BigDecimal.ZERO);
        response.setAveragePrice(null);
        response.setTrades(new ArrayList<>());
        response.setHasMatched(false);
        response.setCompletelyFilled(false);
        response.setPartiallyFilled(false);
        response.setStatus("REJECTED");
        response.setMessage(reason != null ? reason : "Order rejected - price out of valid range");
        response.setProcessedAt(LocalDateTime.now());
        
        return response;
    }
    
    public static OrderProcessingResponse failed(String reason) {
        OrderProcessingResponse response = new OrderProcessingResponse();
        response.setOrderId(null);
        response.setSymbol(null);
        response.setStrategy(null);
        response.setOriginalQuantity(BigDecimal.ZERO);
        response.setFilledQuantity(BigDecimal.ZERO);
        response.setRemainingQuantity(BigDecimal.ZERO);
        response.setAveragePrice(null);
        response.setTrades(new ArrayList<>());
        response.setHasMatched(false);
        response.setCompletelyFilled(false);
        response.setPartiallyFilled(false);
        response.setStatus("ERROR");
        response.setMessage(reason != null ? reason : "Order processing failed");
        response.setProcessedAt(LocalDateTime.now());
        
        return response;
    }
    
    private static List<TradeInfo> convertTradesToInfo(List<Trade> trades) {
        if (trades == null) {
            return new ArrayList<>();
        }
        
        List<TradeInfo> tradeInfos = new ArrayList<>();
        for (Trade trade : trades) {
            TradeInfo info = new TradeInfo(
                trade.getTradeId(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getExecutedAt(),
                "BOT" // Counter-bot trades
            );
            tradeInfos.add(info);
        }
        
        return tradeInfos;
    }
    
    public int getTradeCount() {
        return trades != null ? trades.size() : 0;
    }
    
    public BigDecimal getTotalTradeValue() {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return trades.stream()
            .map(trade -> trade.getPrice().multiply(trade.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public String getStatusDescription() {
        switch (status != null ? status : "UNKNOWN") {
            case "FILLED": return "Order completely executed";
            case "PARTIALLY_FILLED": return "Order partially executed";
            case "ADDED_TO_BOOK": return "Order added to order book";
            case "REJECTED": return "Order rejected";
            case "ERROR": return "Processing error occurred";
            default: return "Unknown status";
        }
    }
}