package com.stofina.app.orderservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trades")
public class Trade {

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
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal buyCommission; //Alıcının ödediği komisyon

    @Column(precision = 19, scale = 4)
    private BigDecimal sellCommission; //Satıcının ödediği komisyon

    private boolean isBotTrade = false;

    private LocalDateTime executedAt; // İşlemin gerçekleştiği zaman

    @Column(length = 64, unique = true)
    private String tradeRef; // Trade referans numarası, UUID formatında


    @PrePersist
    public void onCreate() {
        this.executedAt = LocalDateTime.now();
        this.tradeRef = generateTradeRef();
    }

    public String generateTradeRef() {// Trade referans numarasını UUID formatında oluşturur
        return UUID.randomUUID().toString();
    }

    public BigDecimal getTradeAmount() { // İşlem tutarını hesaplar
        return price.multiply(quantity);
    }

}