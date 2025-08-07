package com.stofina.app.market_data_service.service;

import com.stofina.market_data_service.dto.websocket.PriceUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastPriceUpdate(String symbol, PriceUpdateMessage message) {
        String destination = "/topic/price/" + symbol;
        messagingTemplate.convertAndSend(destination, message);
        log.info("Broadcasted price update for symbol {}: {}", symbol, message);
    }
}
