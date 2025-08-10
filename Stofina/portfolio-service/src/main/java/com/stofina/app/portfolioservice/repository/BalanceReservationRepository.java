package com.stofina.app.portfolioservice.repository;

import com.stofina.app.portfolioservice.enums.ReservationStatus;
import com.stofina.app.portfolioservice.model.BalanceReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceReservationRepository extends JpaRepository<BalanceReservation, Long> {

    List<BalanceReservation> findByAccountId(Long accountId);

    List<BalanceReservation> findByAccountIdAndStatus(Long accountId, ReservationStatus status);
    Optional<BalanceReservation> findByAccountIdAndOrderId(Long accountId, Long orderId);
}
