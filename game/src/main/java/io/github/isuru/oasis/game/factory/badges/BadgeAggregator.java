package io.github.isuru.oasis.game.factory.badges;

import io.github.isuru.oasis.model.Event;

import java.io.Serializable;

class BadgeAggregator implements Serializable {
    private Long userId;
    private Double value = 0.0;
    private Event lastRefEvent;
    private Event firstRefEvent;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    Event getLastRefEvent() {
        return lastRefEvent;
    }

    void setLastRefEvent(Event lastRefEvent) {
        this.lastRefEvent = lastRefEvent;
    }

    Event getFirstRefEvent() {
        return firstRefEvent;
    }

    void setFirstRefEvent(Event firstRefEvent) {
        this.firstRefEvent = firstRefEvent;
    }
}
