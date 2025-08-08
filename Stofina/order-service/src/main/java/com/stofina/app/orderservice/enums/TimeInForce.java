package com.stofina.app.orderservice.enums;

import java.time.LocalDateTime;
import java.time.LocalTime;

public enum TimeInForce {
    DAY("Gün Sonu", "Gün sonuna kadar geçerli"),
    GTC("İptal Edilene Kadar", "Manuel iptal edilene kadar geçerli"),
    IOC("Hemen veya İptal", "Hemen gerçekleşmezse iptal");

    private final String displayName;
    private final String description;

    TimeInForce(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresExpiryDate() {
        return false;
    }

    public LocalDateTime getDefaultExpiry() {
        return switch (this) {
            case DAY -> LocalDateTime.now().with(LocalTime.of(18, 0));
            case GTC -> LocalDateTime.now().plusYears(1);
            case IOC -> LocalDateTime.now().plusMinutes(1);
        };
    }
}