package com.stofina.app.java.market_data_service.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceUpdateMessage {
    private String symbol;
    private double price;
    private double changeAmount;
    private double changePercent;
    private long timestamp;
}