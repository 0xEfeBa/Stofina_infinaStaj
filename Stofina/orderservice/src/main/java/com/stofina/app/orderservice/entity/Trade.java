package com.stofina.app.orderservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades", indexes = {
    @Index(name = "idx_account_executed", columnList = "buyAccountId, executedAt"),
    @Index(name = "idx_account_executed_sell", columnList = "sellAccountId, executedAt"),
    @Index(name = "idx_symbol_executed", columnList = "symbol, executedAt"),
    @Index(name = "idx_buy_order", columnList = "buyOrderId"),
    @Index(name = "idx_sell_order", columnList = "sellOrderId"),
    @Index(name = "idx_tenant_symbol", columnList = "tenantId, symbol")
})
@Data
@EqualsAndHashCode(of = "tradeId")
public class Trade {

    // CHECKPOINT 2.2 - Trade Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradeId;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false)
    private Long buyOrderId;

    @Column(nullable = false)
    private Long sellOrderId;

    @Column(nullable = false)
    private Long buyAccountId;

    @Column(nullable = false)
    private Long sellAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    @NotNull
    @Positive
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull
    @Positive
    private BigDecimal quantity;

    @Column(nullable = false)
    private Boolean isBotTrade = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @Column(nullable = false, unique = true, length = 36)
    private String tradeRef;

    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
        if (tradeRef == null) {
            tradeRef = generateTradeRef();
        }
    }

    public String generateTradeRef() {
        String prefix = symbol != null ? symbol : "UNK";
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TRD_" + prefix + "_" + uuid;
    }

    public BigDecimal getTradeAmount() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(quantity);
    }
}