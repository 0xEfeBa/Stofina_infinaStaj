package com.stofina.app.portfolioservice.repository;

import com.stofina.app.portfolioservice.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    Optional<StockReservation> findByAccountIdAndOrderId(Long accountId, Long orderId);

    List<StockReservation> findByAccountId(Long accountId);
}
