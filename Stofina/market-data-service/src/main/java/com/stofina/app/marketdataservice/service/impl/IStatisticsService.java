package com.stofina.app.marketdataservice.service.impl;

import com.stofina.app.marketdataservice.dto.response.PriceResponse;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface IStatisticsService {
    List<PriceResponse> getAllPrices();
}
