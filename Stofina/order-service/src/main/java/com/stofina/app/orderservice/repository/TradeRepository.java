package com.stofina.app.orderservice.repository;

import com.stofina.app.orderservice.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByBuyAccountIdAndExecutedAtBetween(Long accountId, LocalDateTime start, LocalDateTime end);

    Page<Trade> findBySymbolOrderByExecutedAtDesc(String symbol, Pageable pageable);

    List<Trade> findByBuyOrderIdOrSellOrderId(Long buyOrderId, Long sellOrderId);

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Trade t WHERE t.symbol = :symbol AND DATE(t.executedAt) = :date")
    BigDecimal getDailyVolume(String symbol, LocalDate date);
}