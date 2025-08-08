package com.stofina.app.orderservice.dto.response.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PriceResponse {

    private String symbol;
    private BigDecimal price;
    private Instant timestamp;
    private String source;
    private boolean marketOpen;

}
