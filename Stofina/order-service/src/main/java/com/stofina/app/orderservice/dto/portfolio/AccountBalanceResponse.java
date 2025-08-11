package com.stofina.app.orderservice.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {
    
    private Long accountId;
    private BigDecimal availableBalance;
    private BigDecimal totalBalance;
    private String currency;
    
}