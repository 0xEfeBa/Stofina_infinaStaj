package com.stofina.app.market_data_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketStatusResponse {
    private boolean isOpen;
    private LocalDateTime nextOpenTime;
    private LocalDateTime nextCloseTime;
}
