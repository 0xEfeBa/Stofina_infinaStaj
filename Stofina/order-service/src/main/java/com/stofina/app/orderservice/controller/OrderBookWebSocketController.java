package com.stofina.app.orderservice.controller;

import com.stofina.app.orderservice.dto.response.OrderBookResponse;
import com.stofina.app.orderservice.model.SimpleOrderBookSnapshot;
import com.stofina.app.orderservice.service.DisplayOrderBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class OrderBookWebSocketController {

    @Autowired
    private DisplayOrderBookService displayOrderBookService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/orderbook/subscribe/{symbol}")
    @SendTo("/topic/orderbook/{symbol}")
    public Map<String, Object> subscribeToOrderBook(@DestinationVariable String symbol, @Payload Map<String, Object> message) {
        try {
            String normalizedSymbol = symbol.toUpperCase();
            
            // Get current order book snapshot
            SimpleOrderBookSnapshot snapshot = displayOrderBookService.getDisplaySnapshot(normalizedSymbol);
            OrderBookResponse orderBookResponse = OrderBookResponse.fromSnapshot(snapshot);
            
            // Create WebSocket response
            Map<String, Object> response = Map.of(
                "type", "ORDER_BOOK_UPDATE",
                "payload", Map.of(
                    "symbol", normalizedSymbol,
                    "data", Map.of(
                        "bids", orderBookResponse.getBids(),
                        "asks", orderBookResponse.getAsks()
                    )
                ),
                "timestamp", System.currentTimeMillis()
            );
            
            return response;
        } catch (Exception e) {
            return Map.of(
                "type", "ERROR",
                "payload", Map.of(
                    "message", "Failed to get order book for symbol: " + symbol,
                    "error", e.getMessage()
                ),
                "timestamp", System.currentTimeMillis()
            );
        }
    }

    public void broadcastOrderBookUpdate(String symbol, OrderBookResponse orderBook) {
        Map<String, Object> message = Map.of(
            "type", "ORDER_BOOK_UPDATE",
            "payload", Map.of(
                "symbol", symbol.toUpperCase(),
                "data", Map.of(
                    "bids", orderBook.getBids(),
                    "asks", orderBook.getAsks()
                )
            ),
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/orderbook/" + symbol.toUpperCase(), message);
    }
}