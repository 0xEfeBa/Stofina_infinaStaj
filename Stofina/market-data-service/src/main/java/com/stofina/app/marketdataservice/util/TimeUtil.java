package com.stofina. app.marketdataservice.util;

import com.stofina. app.marketdataservice.constant.Constants;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Set;

// CHECKPOINT 2.10: Time Utility
public final class TimeUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalTime MARKET_OPEN = LocalTime.of(Constants.MarketHours.MARKET_OPEN_HOUR, Constants.MarketHours.MARKET_OPEN_MINUTE);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(Constants.MarketHours.MARKET_CLOSE_HOUR, Constants.MarketHours.MARKET_CLOSE_MINUTE);
    
    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
        MonthDay.of(1, 1),   // Yılbaşı
        MonthDay.of(4, 23),  // Ulusal Egemenlik ve Çocuk Bayramı
        MonthDay.of(5, 1)    // İşçi Bayramı
    );

    private TimeUtil() {
        // Utility class - prevent instantiation
    }

    public static boolean isWeekday(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    public static boolean isMarketHours(LocalTime time) {
        if (time == null) {
            return false;
        }
        return !time.isBefore(MARKET_OPEN) && time.isBefore(MARKET_CLOSE);
    }

    public static LocalDateTime getCurrentMarketTime() {
        return LocalDateTime.now(ZoneId.of(Constants.MarketHours.TIMEZONE));
    }

    public static String formatTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIMESTAMP_FORMATTER);
    }

    public static boolean isMarketDay(LocalDate date) {
        return isWeekday(date) && !isHoliday(date);
    }

    public static boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        return FIXED_HOLIDAYS.contains(MonthDay.from(date));
    }

    public static long getMillisecondsUntilMarketOpen() {
        LocalDateTime now = getCurrentMarketTime();
        LocalDateTime nextOpen;

        LocalDate today = now.toLocalDate();
        LocalDateTime todayOpen = LocalDateTime.of(today, MARKET_OPEN);

        if (now.isBefore(todayOpen) && isMarketDay(today)) {
            nextOpen = todayOpen;
        } else {
            // Find next market day
            LocalDate nextMarketDay = today.plusDays(1);
            while (!isMarketDay(nextMarketDay)) {
                nextMarketDay = nextMarketDay.plusDays(1);
            }
            nextOpen = LocalDateTime.of(nextMarketDay, MARKET_OPEN);
        }

        return Duration.between(now, nextOpen).toMillis();
    }

    public static long getMillisecondsUntilMarketClose() {
        LocalDateTime now = getCurrentMarketTime();
        LocalDate today = now.toLocalDate();
        LocalDateTime todayClose = LocalDateTime.of(today, MARKET_CLOSE);

        if (now.isBefore(todayClose) && isMarketDay(today) && isMarketHours(now.toLocalTime())) {
            return Duration.between(now, todayClose).toMillis();
        }

        return 0; // Market closed - 0 milliseconds until close
    }

    public static String getTimeZone() {
        return Constants.MarketHours.TIMEZONE;
    }
}