package com.stofina.app.orderservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class TradeResponse {

    private Long tradeId;
    private Long orderId;
    private Long accountId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal tradeValue;
    private LocalDateTime executedAt;
} 