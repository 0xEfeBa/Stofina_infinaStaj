package com.stofina.app.orderservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@lombok.Data
public class SimpleStopLossWatcher {
    private Long orderId;           // Orijinal stop-loss order ID
    private String symbol;          // AAPL
    private BigDecimal triggerPrice; // 145₺
    private BigDecimal quantity;    // 100
    private Long accountId;         // 12345
    private Long tenantId;          // Tenant ID
    private LocalDateTime createdAt;
    private LocalDateTime lastCheckAt;
    private int checkCount;
    private boolean triggered = false;
}
