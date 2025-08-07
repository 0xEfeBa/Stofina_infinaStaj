package com.stofina.app.orderservice.entity;


import com.stofina.app.orderservice.enums.OrderSide;
import com.stofina.app.orderservice.enums.OrderStatus;
import com.stofina.app.orderservice.enums.OrderType;
import com.stofina.app.orderservice.enums.TimeInForce;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long tenantId; // Hangi şirkete/ kullanıcıya ait olduğunu belirtir

    @Column(nullable = false)
    private Long accountId; //Emri veren müşterinin id'si

    @Column(nullable = false, length = 10)
    private String symbol; // İşlem sembolü (örn: AAPL, BTCUSDT)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType; // Emrin tipi (örn: LIMIT, MARKET, STOP_LOSS)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side; // Emrin yönü (örn: BUY, SELL)

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity; // Emrin miktarı (örn: 10.5 BTC, 100 AAPL)

    @Column(precision = 19, scale = 4)
    private BigDecimal price; //Sadece LIMIT emirlerde geçerli olan limit fiyat

    @Column(precision = 19, scale = 4)
    private BigDecimal filledQuantity = BigDecimal.ZERO; //Emir gerçekleşmeye başladıysa o ana kadar gerçekleşen miktar.

    @Column(precision = 19, scale = 4)
    private BigDecimal averagePrice; // Emir gerçekleştiğinde ortalama fiyatı tutar

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW; //NEW, ACTIVE, FILLED, CANCELLED, vb.

    @Enumerated(EnumType.STRING)
    private TimeInForce timeInForce; // Emrin geçerlilik süresi (örn: DAY, GTC, IOC)

    @Column(precision = 19, scale = 4)
    private BigDecimal stopPrice; // Stop loss emirleri için tetikleme fiyatı

    private LocalDateTime expiryDate; // Emrin geçerlilik süresi dolma tarihi (örn: 2023-12-31T18:00:00)

    @Column(length = 64, unique = true)
    private String clientOrderId; //Kullanıcının frontend üzerinden oluşturduğu özel emir ID’si

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    private Boolean isBot = false; // Emri bir bot tarafından mı verildiğini belirtir


    public BigDecimal getRemainingQuantity() { // Emir miktarından gerçekleşen miktarı çıkararak kalan miktarı hesaplar
        return quantity.subtract(filledQuantity != null ? filledQuantity : BigDecimal.ZERO);
    }

    public boolean isFullyFilled() { // Emrin tamamen gerçekleşip gerçekleşmediğini kontrol eder
        return filledQuantity != null && quantity != null && filledQuantity.compareTo(quantity) >= 0;
    }

    public boolean canBeCancelled() { // Emrin iptal edilebilir olup olmadığını kontrol eder
        return status.canCancel();
    }

    public boolean canBeUpdated() {
        return status.canUpdate();
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
