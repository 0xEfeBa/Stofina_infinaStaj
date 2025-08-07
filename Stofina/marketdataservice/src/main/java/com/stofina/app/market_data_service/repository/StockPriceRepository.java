package com.stofina.app.market_data_service.repository;

import com.stofina.market_data_service.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, String> {
}
