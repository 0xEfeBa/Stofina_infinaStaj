package com.stofina.app.portfolioservice.dto;

import com.stofina.app.portfolioservice.enums.ReservationStatus;
import com.stofina.app.portfolioservice.enums.ReservationType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BalanceReservationDto {

    private Long id;
    private Long accountId;
    private Long orderId;

    private BigDecimal reservedAmount;
    private ReservationType reservationType;
    private ReservationStatus status;

    private BigDecimal usedAmount;

    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;

    private String description;
}
