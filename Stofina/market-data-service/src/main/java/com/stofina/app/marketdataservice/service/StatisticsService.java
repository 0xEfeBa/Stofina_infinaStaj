package com.stofina.app.marketdataservice.service;

import com.stofina.app.marketdataservice.dto.response.PriceResponse;
import com.stofina.app.marketdataservice.service.impl.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class StatisticsService implements IStatisticsService {

    private static final Map<String, Double> MOCK_PRICES = new HashMap<>();

    static {
        MOCK_PRICES.put("THYAO", 15.5);
        MOCK_PRICES.put("ASELS", 12.3);
        MOCK_PRICES.put("GARAN", 9.8);
    }

    @Override
    public List<PriceResponse> getAllPrices() {
        long now = System.currentTimeMillis();
        List<PriceResponse> prices = new ArrayList<>();
        for (Map.Entry<String, Double> entry : MOCK_PRICES.entrySet()) {
            prices.add(new PriceResponse(entry.getKey(), entry.getValue(),now));
        }
        return prices;
    }
}
