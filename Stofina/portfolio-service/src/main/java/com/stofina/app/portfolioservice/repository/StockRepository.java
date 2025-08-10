package com.stofina.app.portfolioservice.repository;

import com.stofina.app.portfolioservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByAccountId(Long accountId);

    Optional<Stock> findByAccountIdAndSymbol(Long accountId, String symbol);

}
