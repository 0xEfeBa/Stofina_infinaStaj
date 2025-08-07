package com.stofina.app.market_data_service.controller;

import com.stofina.market_data_service.dto.request.SubscriptionRequest;
import com.stofina.market_data_service.dto.websocket.PriceUpdateMessage;
import com.stofina.market_data_service.service.WebSocketBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MarketWebSocketController {

    private final WebSocketBroadcastService broadcastService;

    @MessageMapping("/subscribe")
    public void handleSubscribe(@Payload SubscriptionRequest request, SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getSessionAttributes().put("subscribedSymbol", request.getSymbol());

        PriceUpdateMessage firstPrice = new PriceUpdateMessage(
                request.getSymbol(),
                250.00,
                0.0,
                0.0,
                System.currentTimeMillis()
        );

        broadcastService.broadcastPriceUpdate(request.getSymbol(), firstPrice);
    }

    @MessageMapping("/unsubscribe")
    public void handleUnsubscribe(@Payload SubscriptionRequest request,
                                  SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getSessionAttributes().remove("subscribedSymbol");
    }
}
