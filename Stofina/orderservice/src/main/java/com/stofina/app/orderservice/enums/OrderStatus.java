package com.stofina.app.orderservice.enums;

import java.util.Set;

public enum OrderStatus {
    NEW("Yeni", false),
    PENDING_TRIGGER("Tetikleme Bekliyor", false),
    ACTIVE("Aktif", false),
    PARTIALLY_FILLED("Kısmen Gerçekleşti", false),
    FILLED("Tamamen Gerçekleşti", true),
    CANCELLED("İptal Edildi", true),
    REJECTED("Reddedildi", true),
    EXPIRED("Süresi Doldu", true);

    private final String displayName;
    private final boolean isFinal;

    OrderStatus(String displayName, boolean isFinal) {
        this.displayName = displayName;
        this.isFinal = isFinal;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean canUpdate() {
        return this == ACTIVE || this == PARTIALLY_FILLED;
    }

    public boolean canCancel() {
        return this == NEW || this == ACTIVE || this == PARTIALLY_FILLED || this == PENDING_TRIGGER;
    }

    public boolean isActive() {
        return this == ACTIVE || this == PARTIALLY_FILLED;
    }

    public Set<OrderStatus> getAllowedTransitions() {
        return switch (this) {
            case NEW -> Set.of(ACTIVE, REJECTED, CANCELLED, PENDING_TRIGGER);
            case PENDING_TRIGGER -> Set.of(ACTIVE, CANCELLED, EXPIRED);
            case ACTIVE -> Set.of(PARTIALLY_FILLED, FILLED, CANCELLED, EXPIRED);
            case PARTIALLY_FILLED -> Set.of(FILLED, CANCELLED);
            case FILLED, CANCELLED, REJECTED, EXPIRED -> Set.of();
        };
    }
}