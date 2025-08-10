package com.stofina.app.marketdataservice.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

// CHECKPOINT 2.10: Price Calculation Utility
public final class PriceCalculationUtil {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private PriceCalculationUtil() {
        // Utility class - prevent instantiation
    }

    public static BigDecimal calculateChangeAmount(BigDecimal currentPrice, BigDecimal previousPrice) {
        if (currentPrice == null || previousPrice == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(previousPrice);
    }

    public static BigDecimal calculateChangePercent(BigDecimal currentPrice, BigDecimal previousPrice) {
        if (currentPrice == null || previousPrice == null || previousPrice.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        BigDecimal change = currentPrice.subtract(previousPrice);
        BigDecimal percentage = change.divide(previousPrice, 4, RoundingMode.HALF_UP);
        return percentage.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isWithinDailyLimit(BigDecimal price, BigDecimal defaultPrice, BigDecimal limitPercent) {
        if (price == null || defaultPrice == null || limitPercent == null) {
            return false;
        }

        BigDecimal maxPrice = defaultPrice.multiply(BigDecimal.ONE.add(limitPercent));
        BigDecimal minPrice = defaultPrice.multiply(BigDecimal.ONE.subtract(limitPercent));

        return price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
    }

    public static BigDecimal roundToDecimalPlaces(BigDecimal value, int places) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(places, RoundingMode.HALF_UP);
    }

    public static boolean isPricePositive(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    public static BigDecimal calculateVolatility(BigDecimal price, double volatilityPercent) {
        if (price == null || volatilityPercent <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal volatility = BigDecimal.valueOf(volatilityPercent);
        return price.multiply(volatility).setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatPriceChange(BigDecimal changeAmount, BigDecimal changePercent) {
        if (changeAmount == null || changePercent == null) {
            return "0.00 (0.00%)";
        }

        String sign = changeAmount.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s%s (%s%%)", sign, changeAmount, changePercent);
    }
}