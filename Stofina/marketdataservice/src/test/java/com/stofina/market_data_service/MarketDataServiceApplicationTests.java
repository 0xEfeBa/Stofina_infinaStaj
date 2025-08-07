package com.stofina.app.market_data_service;

import com.stofina.market_data_service.entity.Stock;
import com.stofina.market_data_service.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MarketDataServiceApplicationTests {

	@Autowired
	private StockRepository stockRepository;

	@Test
	void contextLoads() {
		assertNotNull(stockRepository);
	}

	@Test
	void stockTableShouldContainTenRecords() {
		List<Stock> stocks = stockRepository.findAll();

		System.out.println(" Veritabanındaki kayıt sayısı: " + stocks.size());
		stocks.forEach(stock ->
				System.out.println(" - " + stock.getSymbol() + ": " + stock.getCompanyName())
		);

		assertEquals(10, stocks.size(), "Veritabanında tam olarak 10 stock kaydı olmalı");
	}


}
