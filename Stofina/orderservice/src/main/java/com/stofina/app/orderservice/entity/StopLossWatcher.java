package com.stofina.app.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stop_loss_watchers", indexes = {
        @Index(name = "idx_symbol_triggered", columnList = "symbol, triggered"),
        @Index(name = "idx_order_id", columnList = "orderId"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopLossWatcher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long watcherId;
    
    @Column(nullable = false)
    private Long orderId;           // Orijinal stop-loss order ID
    
    @Column(nullable = false, length = 10)
    private String symbol;          // THYAO, GARAN, etc.
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal triggerPrice; // 44.00 TL
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal quantity;    // 100 adet
    
    @Column(nullable = false)
    private Long accountId;         // 11111
    
    @Column(nullable = false)
    private Long tenantId;          // Tenant ID
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime lastCheckAt;
    
    @Column(nullable = false)
    private Integer checkCount = 0;
    
    @Column(nullable = false)
    private Boolean triggered = false;
    
    @Column(nullable = false)
    private Boolean active = true;  // Soft delete için
    
    @Version
    private Long version;
}