package com.stofina.app.orderservice.dto.response.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketStatusResponse {

    private String status;
    private String message;
    private Instant nextOpen;
    private Instant nextClose;

}
