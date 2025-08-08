package com.stofina.app.marketdataservice.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Generic WebSocket message wrapper matching frontend format
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessage<T> {
    private String type;
    private T payload;
    private String timestamp;
    private String id;
    
    public WebSocketMessage(String type, T payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = LocalDateTime.now().toString();
        this.id = "msg-" + System.currentTimeMillis();
    }
}