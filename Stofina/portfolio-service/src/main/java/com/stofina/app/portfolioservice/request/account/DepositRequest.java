package com.stofina.app.portfolioservice.request.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class DepositRequest {

    @NotNull
    private BigDecimal amount;

    private String description;
}
