package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.rules.BadgeRule;

import java.io.Serializable;
import java.util.List;

public class BadgeNotification implements Serializable {

    private List<? extends Event> events;
    private BadgeRule rule;
    private Badge badge;

    public BadgeNotification(List<? extends Event> events, BadgeRule rule, Badge badge) {
        this.events = events;
        this.rule = rule;
        this.badge = badge;
    }

    public List<? extends Event> getEvents() {
        return events;
    }

    public BadgeRule getRule() {
        return rule;
    }

    public Badge getBadge() {
        return badge;
    }
}
