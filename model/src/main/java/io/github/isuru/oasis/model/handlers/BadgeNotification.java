package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.rules.BadgeRule;

import java.io.Serializable;
import java.util.List;

public class BadgeNotification implements Serializable {

    private long userId;
    private List<? extends Event> events;
    private BadgeRule rule;
    private Badge badge;
    private String tag;

    public BadgeNotification(long userId, List<? extends Event> events, BadgeRule rule, Badge badge,
                             String tag) {
        this.events = events;
        this.rule = rule;
        this.badge = badge;
        this.userId = userId;
        this.tag = tag;
    }

    public long getUserId() {
        return userId;
    }

    public String getTag() {
        return tag;
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
