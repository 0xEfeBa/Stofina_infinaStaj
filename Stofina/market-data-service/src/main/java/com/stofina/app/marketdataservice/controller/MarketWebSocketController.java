package com.stofina.app.marketdataservice.controller;

import com.stofina.app.marketdataservice.dto.request.MarketDataSubscription;

import com.stofina.app.marketdataservice.dto.websocket.PriceUpdateMessage;

import com.stofina.app.marketdataservice.dto.websocket.WebSocketMessage;
import com.stofina.app.marketdataservice.service.IWebSocketBroadcastService;
import com.stofina.app.marketdataservice.service.IPriceSimulationService;
import com.stofina.app.marketdataservice.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

import java.util.*;


@Slf4j
@Controller
@RequiredArgsConstructor
public class MarketWebSocketController {

    private final IWebSocketBroadcastService broadcastService;
    private final IPriceSimulationService priceSimulationService;

    @MessageMapping("/subscribe")
    public void handleSubscribe(@Payload WebSocketMessage<MarketDataSubscription> message, SimpMessageHeaderAccessor headerAccessor) {

        MarketDataSubscription subscription = message.getPayload();
        List<String> symbols = subscription.getSymbols();
        
        if (symbols != null && !symbols.isEmpty()) {
            headerAccessor.getSessionAttributes().put("subscribedSymbols", symbols);
            
            // Her symbol için gerçek fiyat verisi gönder
            for (String symbol : symbols) {
                Stock stock = priceSimulationService.getStockBySymbol(symbol);
                if (stock != null) {
                    BigDecimal currentPrice = stock.getCurrentPrice();
                    BigDecimal previousClose = stock.getPreviousClose();
                    BigDecimal change = currentPrice.subtract(previousClose);
                    
                    PriceUpdateMessage priceMessage = new PriceUpdateMessage(symbol, currentPrice.doubleValue(), change.doubleValue(), 0.0, System.currentTimeMillis());
                    broadcastService.broadcastPriceUpdate(symbol, priceMessage);
                }
            }
        }
    }

    @MessageMapping("/unsubscribe")
    public void handleUnsubscribe(@Payload String symbol,
                                  SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getSessionAttributes().remove("subscribedSymbol");
    }
}
