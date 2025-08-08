package com.stofina.app.marketdataservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Market data subscription request matching frontend format
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketDataSubscription {
    private List<String> symbols;
    private Integer updateFrequency = 1000; // Default 1 second
    private Boolean includeVolume = true;
    private Boolean includeExtendedHours = false;
}