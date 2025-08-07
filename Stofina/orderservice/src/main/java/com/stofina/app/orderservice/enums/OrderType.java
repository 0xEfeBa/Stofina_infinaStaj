package com.stofina.app.orderservice.enums;

public enum OrderType {
    MARKET("Market Order", "Piyasa fiyatından anlık işlem"),
    LIMIT("Limit Order", "Belirtilen fiyat veya daha iyisinden işlem"),
    STOP_LOSS("Stop Loss Order", "Zarar durdurma emri");

    private final String displayName;
    private final String description;

    OrderType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresPrice() {
        return this == LIMIT;
    }

    public boolean requiresStopPrice() {
        return this == STOP_LOSS;
    }
}