package com.stofina.app.orderservice.enums;

public enum OrderType {
    LIMIT_BUY("Limit Alış", "Belirtilen fiyat veya daha düşüğünden alış"),
    LIMIT_SELL("Limit Satış", "Belirtilen fiyat veya daha yüksekten satış"),
    MARKET_BUY("Market Alış", "Piyasa fiyatından anlık alış"),
    MARKET_SELL("Market Satış", "Piyasa fiyatından anlık satış"),
    STOP_LOSS_SELL("Stop Loss Satış", "Zarar durdurma satış emri");

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
        return this == LIMIT_BUY || this == STOP_LOSS_SELL;
    }

    public boolean requiresStopPrice() {
        return this == STOP_LOSS_SELL;
    }
    
    public OrderSide getSide() {
        return switch(this) {
            case LIMIT_BUY, MARKET_BUY -> OrderSide.BUY;
            case STOP_LOSS_SELL -> OrderSide.SELL;
        };
    }
    
    public boolean isLimitOrder() {
        return this == LIMIT_BUY;
    }
    
    public boolean isMarketOrder() {
        return this == MARKET_BUY;
    }
    
    public boolean isStopLossOrder() {
        return this == STOP_LOSS_SELL;
    }
}