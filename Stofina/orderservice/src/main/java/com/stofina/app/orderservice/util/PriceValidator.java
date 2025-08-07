package com.stofina.app.orderservice.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceValidator {

    private static final BigDecimal DAILY_LIMIT_PERCENT = new BigDecimal("0.10"); // %10 günlük limit
    private static final BigDecimal REASONABLE_PRICE_MIN = new BigDecimal("0.01"); // Minimum makul fiyat
    private static final BigDecimal REASONABLE_PRICE_MAX = new BigDecimal("1000000"); // Maksimum makul fiyat

    public static boolean isWithinDailyLimit(BigDecimal current, BigDecimal order) {
        if (current == null || order == null) return false;
        BigDecimal deviation = calculateDeviation(current, order).abs();
        return deviation.compareTo(DAILY_LIMIT_PERCENT) <= 0;
    }

    public static boolean isReasonablePrice(BigDecimal price) {
        if (price == null) return false;
        return price.compareTo(REASONABLE_PRICE_MIN) >= 0 && price.compareTo(REASONABLE_PRICE_MAX) <= 0;
    }

    public static BigDecimal calculateDeviation(BigDecimal base, BigDecimal compare) {
        if (base == null || compare == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return compare.subtract(base)
                .divide(base, 6, RoundingMode.HALF_UP);
    }
}
