package com.stofina.app.marketdataservice.entity;

import com.stofina.app.marketdataservice.enums.StockStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "stocks",
        indexes = {
                @Index(name = "idx_symbol", columnList = "symbol"),
                @Index(name = "idx_current_price", columnList = "currentPrice")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_symbol", columnNames = {"symbol"}),
                @UniqueConstraint(name = "uc_stock_name", columnNames = {"stockName"}),
                @UniqueConstraint(name = "uc_isin_code", columnNames = {"isinCode"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String stockName;

    @Column(nullable = false, length = 12)
    private String isinCode;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 50)
    private String equityMarket;

    @Column(nullable = false, length = 50)
    private String exchange;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyHigh;

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyLow;

    @Column(precision = 10, scale = 2)
    private BigDecimal previousClose;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StockStatus status;

    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastUpdated = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
}
