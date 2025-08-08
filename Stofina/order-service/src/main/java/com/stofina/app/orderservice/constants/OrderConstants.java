package com.stofina.app.orderservice.constants;

public final class OrderConstants {

    private OrderConstants() {
        // Utility class
    }

    public static final class Market {
        public static final String MARKET_OPEN_TIME = "09:30";
        public static final String MARKET_CLOSE_TIME = "18:00";
        public static final String TIMEZONE = "Europe/Istanbul";
        
        private Market() {}
    }

    public static final class StopLoss {
        public static final String BASE_PATH = "/api/stop-loss";

        public static final String ADD = "/add";
        public static final String CHECK = "/check";
        public static final String ALL = "/all";
        public static final String IS_WATCHING = "/is-watching/{orderId}";
        public static final String REMOVE = "/{orderId}";

        private StopLoss() {
        }
    }
}