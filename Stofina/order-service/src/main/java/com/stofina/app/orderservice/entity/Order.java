package com.stofina.app.orderservice.entity;

import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.enums.TimeInForce;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_account_status", columnList = "accountId, status"),
    @Index(name = "idx_symbol_status", columnList = "symbol, status"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_tenant_symbol", columnList = "tenantId, symbol")
})
@Data
@EqualsAndHashCode(of = "orderId")
public class Order {

    // CHECKPOINT 2.1 - Core Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 10)
    @Pattern(regexp = "^[A-Z]{4,6}$")
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Column(nullable = false, precision = 19, scale = 2)
    @Positive
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4)
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @Column(precision = 19, scale = 2)
    @PositiveOrZero
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    @PositiveOrZero
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeInForce timeInForce = TimeInForce.DAY;

    @Column(precision = 19, scale = 4)
    @DecimalMin(value = "0.01")
    private BigDecimal stopPrice;

    private LocalDateTime expiryDate;

    @Column(length = 50)
    @Pattern(regexp = "^[A-Z0-9_-]{1,50}$")
    private String clientOrderId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Column(nullable = false)
    private Boolean isBot = false;


    public BigDecimal getRemainingQuantity() {
        if (quantity == null || filledQuantity == null) {
            return BigDecimal.ZERO;
        }
        return quantity.subtract(filledQuantity);
    }

    public boolean isFullyFilled() {
        if (filledQuantity == null || quantity == null) {
            return false;
        }
        return filledQuantity.compareTo(quantity) >= 0;
    }

    public boolean canBeCancelled() {
        return status != null && status.canCancel();
    }

    public boolean canBeUpdated() {
        return status != null && status.canUpdate();
    }


    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        
        if (expiryDate == null && timeInForce != null) {
            expiryDate = timeInForce.getDefaultExpiry();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}