package com.stofina.app.market_data_service.service.interfaces;

import com.stofina.market_data_service.dto.response.PriceResponse;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface IStatisticsService {
    List<PriceResponse> getAllPrices();
}
