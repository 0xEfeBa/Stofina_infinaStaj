package com.stofina.app.portfolioservice.request.account;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferStockRequest {

    @NotNull(message = "From account ID must not be null")
    private Long fromAccountId;

    @NotNull(message = "To account ID must not be null")
    private Long toAccountId;

    @NotBlank(message = "Symbol must not be blank")
    private String symbol;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "Description must not be blank")
    private String description;
}
