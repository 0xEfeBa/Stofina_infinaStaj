package com.stofina.app.market_data_service.service;

import com.stofina.app.market_data_service.dto.websocket.PriceUpdateMessage;
import java.time.LocalDateTime;

public interface IWebSocketBroadcastService {
    
   
    void broadcastPriceUpdate(String symbol, PriceUpdateMessage message);
    
  
    void broadcastToAll(Object message);
    
  
    void sendToUser(String userId, Object message);
    
  
    void broadcastToTopic(String topic, Object message);
    
    
    boolean isConnectionActive();
    
    /**
     * Piyasa durumu mesajını yayınlar
     * @param status Piyasa durumu
     * @param nextOpenTime Sonraki açılış zamanı
     * @param nextCloseTime Sonraki kapanış zamanı
     */
    void broadcastMarketStatus(String status, LocalDateTime nextOpenTime, LocalDateTime nextCloseTime);
}
