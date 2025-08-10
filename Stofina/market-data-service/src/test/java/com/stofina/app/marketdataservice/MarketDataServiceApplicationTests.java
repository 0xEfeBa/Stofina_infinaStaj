package com.stofina.app.marketdataservice;

import com.stofina.app.marketdataservice.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MarketDataServiceApplicationTests {

	@Autowired
	private StockRepository stockRepository;

	@Test
	void contextLoads() {
		assertNotNull(stockRepository);
	}



}
