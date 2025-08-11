package com.stofina.app.orderservice.controller;

import com.stofina.app.orderservice.entity.Trade;
import com.stofina.app.orderservice.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class TradeWebSocketController {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/trades/subscribe")
    @SendTo("/topic/trades/{symbol}")
    public Map<String, Object> subscribeToTrades(@Payload Map<String, Object> message) {
        try {
            String symbol = message.get("symbol").toString().toUpperCase();
            int maxHistory = Integer.parseInt(message.getOrDefault("maxHistory", 50).toString());
            
            // Get recent trades for the symbol
            Page<Trade> tradesPage = tradeRepository.findBySymbolOrderByExecutedAtDesc(
                symbol, 
                PageRequest.of(0, maxHistory, Sort.by(Sort.Direction.DESC, "executedAt"))
            );
            List<Trade> recentTrades = tradesPage.getContent();
            
            // Create WebSocket response
            List<Map<String, Object>> tradeList = recentTrades.stream()
                .map(trade -> {
                    Map<String, Object> tradeMap = new HashMap<>();
                    tradeMap.put("id", trade.getTradeId());
                    tradeMap.put("symbol", trade.getSymbol());
                    tradeMap.put("price", trade.getPrice());
                    tradeMap.put("quantity", trade.getQuantity());
                    tradeMap.put("side", trade.getBuyOrderId() != null ? "BUY" : "SELL");
                    tradeMap.put("timestamp", trade.getExecutedAt().toString());
                    return tradeMap;
                })
                .toList();

            Map<String, Object> response = Map.of( 
                "type", "TRADE_HISTORY",
                "payload", Map.of(
                    "symbol", symbol,
                    "trades", tradeList
                ),
                "timestamp", System.currentTimeMillis()
            );
            
            return response;
        } catch (Exception e) {
            return Map.of(
                "type", "ERROR",
                "payload", Map.of(
                    "message", "Failed to get trade history",
                    "error", e.getMessage()
                ),
                "timestamp", System.currentTimeMillis()
            );
        }
    }

    public void broadcastTradeExecution(Trade trade) {
        Map<String, Object> message = Map.of(
            "type", "TRADE_EXECUTED",
            "payload", Map.of(
                "trade", Map.of(
                    "id", trade.getTradeId(),
                    "symbol", trade.getSymbol(),
                    "price", trade.getPrice(),
                    "quantity", trade.getQuantity(),
                    "timestamp", trade.getExecutedAt().toString()
                )
            ),
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/trades/" + trade.getSymbol().toUpperCase(), message);
    }
}