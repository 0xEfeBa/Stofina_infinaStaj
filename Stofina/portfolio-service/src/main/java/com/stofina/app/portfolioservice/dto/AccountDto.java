package com.stofina.app.portfolioservice.dto;

import com.stofina.app.portfolioservice.enums.AccountStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountDto {

    private Long id;
    private Long customerId;
    private String accountNumber;
    private AccountStatus status;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private BigDecimal reservedBalance;
    private BigDecimal withdrawableBalance;
    private List<StockDto> stocks;
}
