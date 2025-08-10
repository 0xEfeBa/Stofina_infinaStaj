package com.stofina.app.portfolioservice.model;

import com.stofina.app.portfolioservice.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.stofina.app.portfolioservice.enums.ReservationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "stock_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;
    private Long orderId;
    private String symbol;
    private Integer reservedQuantity;

    @Enumerated(EnumType.STRING)
    private ReservationType reservationType;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Integer usedQuantity;

    private LocalDateTime reservationDate;

    private String description;
}
