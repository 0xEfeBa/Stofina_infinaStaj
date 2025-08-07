package com.stofina.app.orderservice.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceValidator {

    private static final BigDecimal DAILY_LIMIT_PERCENT = new BigDecimal("0.10"); // %10 günlük limit
    private static final BigDecimal REASONABLE_PRICE_MIN = new BigDecimal("0.01"); // Minimum makul fiyat
    private static final BigDecimal REASONABLE_PRICE_MAX = new BigDecimal("1000000"); // Maksimum makul fiyat

    /**
     * Günlük limit içinde mi? (current price'a göre order fiyatı %10’dan fazla sapmamalı)
     */
    public static boolean isWithinDailyLimit(BigDecimal current, BigDecimal order) {
        if (current == null || order == null) return false;
        BigDecimal deviation = calculateDeviation(current, order).abs();
        return deviation.compareTo(DAILY_LIMIT_PERCENT) <= 0;
    }

    /**
     * Fiyat makul mü? (0.01 ile 1,000,000 arası gibi)
     */
    public static boolean isReasonablePrice(BigDecimal price) {
        if (price == null) return false;
        return price.compareTo(REASONABLE_PRICE_MIN) >= 0 && price.compareTo(REASONABLE_PRICE_MAX) <= 0;
    }

    /**
     * İki fiyat arasındaki sapmayı (oran olarak) hesapla
     * Örnek: base=100, compare=110 → 0.10 (yani %10 sapma)
     */
    public static BigDecimal calculateDeviation(BigDecimal base, BigDecimal compare) {
        if (base == null || compare == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return compare.subtract(base)
                .divide(base, 6, RoundingMode.HALF_UP);
    }
}
