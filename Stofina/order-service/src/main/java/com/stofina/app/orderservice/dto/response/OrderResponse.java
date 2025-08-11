package com.stofina.app.orderservice.dto.response;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.enums.TimeInForce;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderResponse {
    
    // CHECKPOINT 5.3 - Order Response DTO
    private Long orderId;
    private Long accountId;
    private Long tenantId;
    private String symbol;
    private OrderType orderType;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal averagePrice;
    private OrderStatus status;
    private TimeInForce timeInForce;
    private BigDecimal stopPrice;
    private LocalDateTime expiryDate;
    private String clientOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isBot;
    
    
    public static OrderResponse fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        return new OrderResponse(
            order.getOrderId(),
            order.getAccountId(),
            order.getTenantId(),
            order.getSymbol(),
            order.getOrderType(),
            order.getSide(),
            order.getQuantity(),
            order.getPrice(),
            order.getFilledQuantity(),
            order.getRemainingQuantity(),
            order.getAveragePrice(),
            order.getStatus(),
            order.getTimeInForce(),
            order.getStopPrice(),
            order.getExpiryDate(),
            order.getClientOrderId(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getIsBot()
        );
    }
    
    public boolean isFullyFilled() {
        return filledQuantity != null && quantity != null && 
               filledQuantity.compareTo(quantity) >= 0;
    }
    
    public boolean isActive() {
        return status != null && status.isActive();
    }
}