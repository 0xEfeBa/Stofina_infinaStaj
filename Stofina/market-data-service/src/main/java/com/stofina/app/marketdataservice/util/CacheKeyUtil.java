package com.stofina.app.marketdataservice.util;


public final class CacheKeyUtil {

    private CacheKeyUtil(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static String buildPriceKey(String symbol) {
        return "price:" + symbol;
    }

    public static String buildDailyStatsKey(String symbol) {
        return "daily_stats:" + symbol;
    }

    public static String buildSubscribersKey(String symbol) {
        return "subscribers:" + symbol;
    }
}