package com.stofina.app.orderservice.enums;

public enum OrderSide {
    BUY("Alış", "#28a745"),
    SELL("Satış", "#dc3545");

    private final String turkishName;
    private final String color;

    OrderSide(String turkishName, String color) {
        this.turkishName = turkishName;
        this.color = color;
    }

    public String getTurkishName() {
        return turkishName;
    }

    public String getColor() {
        return color;
    }

    public OrderSide getOpposite() {
        return this == BUY ? SELL : BUY;
    }
}