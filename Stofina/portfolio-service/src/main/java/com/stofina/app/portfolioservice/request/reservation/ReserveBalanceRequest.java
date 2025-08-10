package com.stofina.app.portfolioservice.request.reservation;

import com.stofina.app.portfolioservice.enums.ReservationType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveBalanceRequest {

    @NotNull
    private Long accountId;

    @NotNull
    private Long orderId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal reservedAmount;

    @NotNull
    private ReservationType reservationType;

    private String description;
}
