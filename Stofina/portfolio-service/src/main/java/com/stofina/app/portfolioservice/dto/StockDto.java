package com.stofina.app.portfolioservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StockDto {

    private Long id;
    private Long accountId;

    private String symbol;
    private Integer quantity;
    private BigDecimal averageCost;
}
