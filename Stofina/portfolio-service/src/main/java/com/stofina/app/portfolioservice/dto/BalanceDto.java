package com.stofina.app.portfolioservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDto {
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private BigDecimal reservedBalance;
    private BigDecimal withdrawableBalance;
    private BigDecimal restrictedBalance;
}
