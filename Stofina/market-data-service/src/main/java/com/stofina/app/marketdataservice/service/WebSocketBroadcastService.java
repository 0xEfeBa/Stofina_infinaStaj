package com.stofina.app.marketdataservice.service;

import com.stofina.app.marketdataservice.constant.Constants;
import com.stofina.app.marketdataservice.dto.websocket.PriceUpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// CHECKPOINT 2.6: WebSocket Broadcasting Service
@Service
public class WebSocketBroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketBroadcastService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastPriceUpdate(String symbol, BigDecimal price, BigDecimal changeAmount, BigDecimal changePercent) {
        // PriceUpdateMessage DTO kullan
        PriceUpdateMessage priceUpdateMessage = new PriceUpdateMessage(
            symbol, 
            price, 
            changeAmount, 
            changePercent, 
            LocalDateTime.now(), 
            "PRICE_UPDATE"
        );
        
        // Tüm client'lara genel broadcast
        messagingTemplate.convertAndSend(Constants.WebSocket.PRICE_TOPIC, priceUpdateMessage);
        
        // Specific symbol topic'ine broadcast (test.html için)
        String symbolTopic = Constants.WebSocket.SYMBOL_TOPIC_PREFIX + symbol;
        messagingTemplate.convertAndSend(symbolTopic, priceUpdateMessage);
        
        logger.debug("Broadcasted price update for {}: {} ({}%)", symbol, price, changePercent);
    }

    public void broadcastMarketStatus(String status, LocalDateTime nextOpenTime, LocalDateTime nextCloseTime) {
        Map<String, Object> marketStatusMessage = createMarketStatusMessage(status, nextOpenTime, nextCloseTime);
        
        messagingTemplate.convertAndSend(Constants.WebSocket.MARKET_STATUS_TOPIC, marketStatusMessage);
    }

    public void sendPersonalMessage(String sessionId, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(sessionId, destination, message);
    }

    public void broadcastAllPrices(List<Map<String, Object>> priceList) {
        for (Map<String, Object> priceData : priceList) {
            String symbol = (String) priceData.get("symbol");
            BigDecimal price = (BigDecimal) priceData.get("price");
            BigDecimal changeAmount = (BigDecimal) priceData.get("changeAmount");
            BigDecimal changePercent = (BigDecimal) priceData.get("changePercent");
            
            broadcastPriceUpdate(symbol, price, changeAmount, changePercent);
        }
    }

    private Map<String, Object> createPriceUpdateMessage(String symbol, BigDecimal price, BigDecimal changeAmount, BigDecimal changePercent) {
        Map<String, Object> message = new HashMap<>();
        message.put("symbol", symbol);
        message.put("price", price);
        message.put("changeAmount", changeAmount);
        message.put("changePercent", changePercent);
        message.put("timestamp", LocalDateTime.now());
        message.put("type", "PRICE_UPDATE");
        return message;
    }

    private Map<String, Object> createMarketStatusMessage(String status, LocalDateTime nextOpenTime, LocalDateTime nextCloseTime) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", status);
        message.put("nextOpenTime", nextOpenTime);
        message.put("nextCloseTime", nextCloseTime);
        message.put("timestamp", LocalDateTime.now());
        message.put("type", "MARKET_STATUS");
        return message;
    }

    public void broadcastConnectionInfo(String sessionId, int totalConnections) {
        Map<String, Object> connectionInfo = new HashMap<>();
        connectionInfo.put("sessionId", sessionId);
        connectionInfo.put("totalConnections", totalConnections);
        connectionInfo.put("timestamp", LocalDateTime.now());
        connectionInfo.put("type", "CONNECTION_INFO");
        
        messagingTemplate.convertAndSend(Constants.WebSocket.CONNECTION_TOPIC, connectionInfo);
    }

    public void broadcastErrorMessage(String error, String details) {
        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("error", error);
        errorMessage.put("details", details);
        errorMessage.put("timestamp", LocalDateTime.now());
        errorMessage.put("type", "ERROR");
        
        messagingTemplate.convertAndSend(Constants.WebSocket.ERROR_TOPIC, errorMessage);
    }

    /**
     * Send generic message to specific topic (for new WebSocketMessage format)
     */
    public void sendToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend(topic, message);
        logger.debug("Sent message to topic: {}", topic);
    }
}

