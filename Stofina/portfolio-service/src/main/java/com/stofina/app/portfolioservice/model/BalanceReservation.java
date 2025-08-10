package com.stofina.app.portfolioservice.model;

import com.stofina.app.portfolioservice.enums.AccountStatus;
import com.stofina.app.portfolioservice.enums.ReservationStatus;
import com.stofina.app.portfolioservice.enums.ReservationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "balance_reservations")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BalanceReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    private Long orderId;

    @Column(precision = 19, scale = 4)
    private BigDecimal reservedAmount;

    @Enumerated(EnumType.STRING)
    private ReservationType reservationType;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(precision = 19, scale = 4)
    private BigDecimal usedAmount;

    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;

    private String description;
}
