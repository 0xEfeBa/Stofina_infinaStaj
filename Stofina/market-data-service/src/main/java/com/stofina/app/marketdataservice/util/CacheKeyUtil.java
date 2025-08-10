package com.stofina.app.marketdataservice.util;

import com.stofina. app.marketdataservice.constant.Constants;

public final class CacheKeyUtil {

    private CacheKeyUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String buildPriceKey(String symbol) {
        return Constants.Cache.PRICE_KEY_PREFIX + symbol.toUpperCase();
    }

    // Örn: session:ws-xyz
    public static String buildSessionKey(String sessionId) {
        return Constants.Cache.SESSION_KEY_PREFIX + sessionId;
    }

    // Örn: subscribers:AKBNK
    public static String buildSubscribersKey(String symbol) {
        return Constants.Cache.SUBSCRIBERS_KEY_PREFIX + symbol.toUpperCase();
    }

    // Örn: dailyStats:TUPRS
    public static String buildDailyStatsKey(String symbol) {
        return Constants.Cache.DAILY_STATS_KEY_PREFIX + symbol.toUpperCase();
    }


}
