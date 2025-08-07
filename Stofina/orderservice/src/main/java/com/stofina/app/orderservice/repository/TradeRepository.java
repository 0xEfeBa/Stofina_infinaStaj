package com.stofina.app.orderservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByAccountIdAndExecutedAtBetween(Long accountId, LocalDateTime start, LocalDateTime end);

    Page<Trade> findBySymbolOrderByExecutedAtDesc(String symbol, Pageable pageable);

    List<Trade> findByBuyOrderIdOrSellOrderId(Long buyOrderId, Long sellOrderId);

    @Query("")
    BigDecimal getDailyVolume(String symbol, LocalDate date);
}