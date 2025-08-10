package com.stofina.app.portfolioservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents the currently withdrawable balance of an account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawableBalanceDto {
    private BigDecimal withdrawableAmount;
    private String note;
}
