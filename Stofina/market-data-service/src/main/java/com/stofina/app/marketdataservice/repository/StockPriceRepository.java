package com.stofina.app.marketdataservice.repository;

import com.stofina.app.marketdataservice.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, String> {
}
