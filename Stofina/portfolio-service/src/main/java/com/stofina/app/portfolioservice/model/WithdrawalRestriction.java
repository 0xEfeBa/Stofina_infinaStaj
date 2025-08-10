package com.stofina.app.portfolioservice.model;

import com.stofina.app.portfolioservice.enums.RestrictionStatus;
import com.stofina.app.portfolioservice.enums.RestrictionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_restrictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WithdrawalRestriction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    private LocalDate tradeDate;

    private Long orderId;

    private LocalDateTime settlementDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal restrictedAmount;

    @Enumerated(EnumType.STRING)
    private RestrictionStatus status;

    @Enumerated(EnumType.STRING)
    private RestrictionType restrictionType;

    private Integer transactionCount;

    private String description;
}
