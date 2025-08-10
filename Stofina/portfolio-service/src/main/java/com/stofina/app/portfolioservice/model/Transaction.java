package com.stofina.app.portfolioservice.model;

import com.stofina.app.portfolioservice.enums.SettlementStatus;
import com.stofina.app.portfolioservice.enums.TransactionStatus;
import com.stofina.app.portfolioservice.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;
    private String symbol;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
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
    @PrePersist
    public void prePersist() {
        if (fulfilledQuantity == null) {
            this.fulfilledQuantity = 0;
        }
    }
}
