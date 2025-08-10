package com.stofina.app.portfolioservice.dto;

import com.stofina.app.portfolioservice.enums.SettlementStatus;
import com.stofina.app.portfolioservice.enums.TransactionStatus;
import com.stofina.app.portfolioservice.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long accountId;
    private String symbol;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private SettlementStatus settlementStatus;
    private BigDecimal amount;
    private BigDecimal price;
    private Integer quantity;
    private Integer fulfilledQuantity;
    private LocalDateTime tradeDate;
    private LocalDateTime settlementDate;
    private BigDecimal balanceBeforeTransaction;
    private BigDecimal balanceAfterTransaction;
    private Long orderId;
    private String description;
}
