package com.stofina.app.market_data_service.controller;

import com.stofina.app.market_data_service.dto.request.SubscriptionRequest;
import com.stofina.app.market_data_service.dto.websocket.PriceUpdateMessage;
import com.stofina.app.market_data_service.service.WebSocketBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MarketWebSocketController {

    private final WebSocketBroadcastService broadcastService;

    @MessageMapping("/subscribe")
    public void handleSubscribe(@Payload SubscriptionRequest request, SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getSessionAttributes().put("subscribedSymbol", request.getSymbol());

        broadcastService.broadcastPriceUpdate(
                request.getSymbol(), 
                BigDecimal.valueOf(250.00),
                BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0)
        );
    }

    @MessageMapping("/unsubscribe")
    public void handleUnsubscribe(@Payload SubscriptionRequest request,
                                  SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getSessionAttributes().remove("subscribedSymbol");
    }
}