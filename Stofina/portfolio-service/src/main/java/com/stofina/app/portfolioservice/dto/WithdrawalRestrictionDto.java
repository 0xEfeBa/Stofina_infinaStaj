package com.stofina.app.portfolioservice.dto;

import com.stofina.app.portfolioservice.enums.RestrictionStatus;
import com.stofina.app.portfolioservice.enums.RestrictionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WithdrawalRestrictionDto {

    private Long id;
    private Long accountId;

    private LocalDate tradeDate;
    private LocalDateTime settlementDate;

    private BigDecimal restrictedAmount;

    private RestrictionStatus status;
    private RestrictionType restrictionType;

    private Integer transactionCount;
    private String description;
}
