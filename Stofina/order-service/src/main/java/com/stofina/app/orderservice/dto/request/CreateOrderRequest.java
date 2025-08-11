package com.stofina.app.orderservice.dto.request;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.enums.TimeInForce;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {
    
    // CHECKPOINT 5.3 - Order Creation Request DTO
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotBlank(message = "Symbol is required")
    @Pattern(regexp = "^[A-Z]{1,10}$", message = "Symbol must be 1-10 uppercase letters")
    private String symbol;
    
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be a positive integer")
    private Integer quantity;
    
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;
    
    @DecimalMin(value = "0.01", message = "Stop price must be at least 0.01")
    private BigDecimal stopPrice;
    
    private TimeInForce timeInForce = TimeInForce.DAY;
    
    private LocalDateTime expiryDate;
    
    @Size(max = 50, message = "Client order ID cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]*$", message = "Client order ID can only contain uppercase letters, numbers, underscore and dash")
    private String clientOrderId;
    
    // TODO: ENTEGRASYON SIRASINDA KALDIRILACAK - Test purpose field
    private Long tenantId = 1L; // Default test tenant
    
    
    // Validation helper methods
    public boolean isMarketOrder() {
        return OrderType.MARKET_BUY.equals(orderType);
    }
    
    public boolean isLimitOrder() {
        return OrderType.LIMIT_BUY.equals(orderType);
    }
    
    public boolean isStopLossOrder() {
        return OrderType.STOP_LOSS_SELL.equals(orderType);
    }
    
    public boolean requiresPrice() {
        return orderType != null && orderType.requiresPrice();
    }
    
    public boolean requiresStopPrice() {
        return orderType != null && orderType.requiresStopPrice();
    }
    
}