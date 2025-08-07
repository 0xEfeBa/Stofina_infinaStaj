package com.stofina.app.orderservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class OrderResponse {

    private Long orderId;
    private Long accountId;
    private String symbol;
    private String orderType;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal averagePrice;
    private String status;
    private String timeInForce;
    private BigDecimal stopPrice;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TradeResponse> trades;
}
