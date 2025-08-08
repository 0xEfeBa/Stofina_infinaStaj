package com.stofina.app.marketdataservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceResponse {
    private String symbol;
    private double price;
    private long timestamp;
}
