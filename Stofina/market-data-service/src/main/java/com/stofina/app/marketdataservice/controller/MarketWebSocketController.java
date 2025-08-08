package com.stofina.app.marketdataservice.controller;

import com.stofina.app.marketdataservice.dto.request.SubscriptionRequest;
import com.stofina.app.marketdataservice.dto.websocket.PriceUpdateMessage;
import com.stofina.app.marketdataservice.service.IWebSocketBroadcastService;
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

    private final IWebSocketBroadcastService broadcastService;

    @MessageMapping("/subscribe")
    public void handleSubscribe(@Payload SubscriptionRequest request, SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getSessionAttributes().put("subscribedSymbol", request.getSymbol());

        PriceUpdateMessage message = new PriceUpdateMessage(
              broadcastService.broadcastPriceUpdate(
               BigDecimal.valueOf(250.00),
                BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0)
        ));

    }

    @MessageMapping("/unsubscribe")
    public void handleUnsubscribe(@Payload SubscriptionRequest request,
                                  SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getSessionAttributes().remove("subscribedSymbol");
    }
}