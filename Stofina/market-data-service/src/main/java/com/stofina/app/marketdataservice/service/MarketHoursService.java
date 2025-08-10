package com.stofina.app.marketdataservice.service;

import com.stofina.app.marketdataservice.constant.Constants;
import org.springframework.stereotype.Service;

import java.time.*;

// CHECKPOINT 2.5: Market Hours Business Logic
@Service
public class MarketHoursService {

    public boolean isMarketOpen() {
        // TEST MODU: Market her zaman açık
        return true;
        
        /* NORMAL MOD (YORUM OLARAK BIRAKILDI)
        ZoneId istanbulZone = ZoneId.of(Constants.MarketHours.TIMEZONE);
        LocalTime currentTime = LocalTime.now(istanbulZone);
        LocalDate currentDate = LocalDate.now(istanbulZone);
        DayOfWeek currentDay = currentDate.getDayOfWeek();

        if (isWeekend(currentDay)) {
            return false;
        }

        LocalTime openTime = LocalTime.of(Constants.MarketHours.MARKET_OPEN_HOUR, Constants.MarketHours.MARKET_OPEN_MINUTE);
        LocalTime closeTime = LocalTime.of(Constants.MarketHours.MARKET_CLOSE_HOUR, Constants.MarketHours.MARKET_CLOSE_MINUTE);

        return !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);
        */
    }

    public boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public boolean isWeekend() {
        LocalDate currentDate = LocalDate.now(ZoneId.of(Constants.MarketHours.TIMEZONE));
        return isWeekend(currentDate.getDayOfWeek());
    }

    public LocalDateTime getNextOpenTime() {
        ZoneId istanbulZone = ZoneId.of(Constants.MarketHours.TIMEZONE);
        LocalDateTime now = LocalDateTime.now(istanbulZone);
        
        if (isMarketOpen()) {
            return null; // Market zaten açık
        }

        LocalDate today = now.toLocalDate();
        LocalTime openTime = LocalTime.of(Constants.MarketHours.MARKET_OPEN_HOUR, Constants.MarketHours.MARKET_OPEN_MINUTE);
        LocalDateTime todayOpen = LocalDateTime.of(today, openTime);

        // Bugün henüz açılmadıysa
        if (now.isBefore(todayOpen) && !isWeekend()) {
            return todayOpen;
        }

        // Sonraki iş günü pazartesiyi bul
        LocalDate nextBusinessDay = today.plusDays(1);
        while (isWeekend(nextBusinessDay.getDayOfWeek())) {
            nextBusinessDay = nextBusinessDay.plusDays(1);
        }

        return LocalDateTime.of(nextBusinessDay, openTime);
    }

    public LocalDateTime getNextCloseTime() {
        if (!isMarketOpen()) {
            return null; // Market zaten kapalı
        }

        LocalDate today = LocalDate.now(ZoneId.of(Constants.MarketHours.TIMEZONE));
        LocalTime closeTime = LocalTime.of(Constants.MarketHours.MARKET_CLOSE_HOUR, Constants.MarketHours.MARKET_CLOSE_MINUTE);
        
        return LocalDateTime.of(today, closeTime);
    }

    public boolean isMarketHours(LocalDateTime dateTime) {
        ZoneId istanbulZone = ZoneId.of(Constants.MarketHours.TIMEZONE);
        LocalDateTime istanbulDateTime = dateTime.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(istanbulZone)
            .toLocalDateTime();

        DayOfWeek dayOfWeek = istanbulDateTime.getDayOfWeek();
        if (isWeekend(dayOfWeek)) {
            return false;
        }

        LocalTime time = istanbulDateTime.toLocalTime();
        LocalTime openTime = LocalTime.of(Constants.MarketHours.MARKET_OPEN_HOUR, Constants.MarketHours.MARKET_OPEN_MINUTE);
        LocalTime closeTime = LocalTime.of(Constants.MarketHours.MARKET_CLOSE_HOUR, Constants.MarketHours.MARKET_CLOSE_MINUTE);

        return !time.isBefore(openTime) && time.isBefore(closeTime);
    }

    public String getMarketStatus() {
        if (isMarketOpen()) {
            return "OPEN";
        } else if (isWeekend()) {
            return "WEEKEND";
        } else {
            return "CLOSED";
        }
    }
}