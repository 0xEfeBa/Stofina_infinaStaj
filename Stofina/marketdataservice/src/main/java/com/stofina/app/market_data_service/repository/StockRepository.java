package com.stofina.app.market_data_service.repository;

import com.stofina.app.market_data_service.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findAllByOrderBySymbolAsc();

    
    List<Stock> findBySymbolIn(List<String> symbols);

   
    List<Stock> findByCurrentPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<Stock> findBySymbolStartingWith(String prefix);
    
    boolean existsBySymbol(String symbol);
}