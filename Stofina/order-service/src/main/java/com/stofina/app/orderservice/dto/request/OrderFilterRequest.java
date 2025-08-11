package com.stofina.app.orderservice.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class OrderFilterRequest {

    private Long accountId;
    private String symbol;
    private String orderType;
    private String side;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private boolean includeStopOrders = false;

    // pagination
    private int page = 0;
    private int size = 10;
}
