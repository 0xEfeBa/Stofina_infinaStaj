package com.stofina.app.portfolioservice.request.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferMoneyRequest {

    @NotNull(message = "From account Number must not be null")
    private String fromAccountNumber;

    @NotNull(message = "To account Number must not be null")
    private String  toAccountNumber;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Description must not be blank")
    private String description;
}
