package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.rules.PointRule;

import java.io.Serializable;
import java.util.List;

public class PointNotification implements Serializable {

    private long userId;
    private List<? extends Event> events;
    private PointRule rule;
    private double amount;
    private String tag;

    public PointNotification(long userId, List<? extends Event> events, PointRule rule, double amount) {
        this.userId = userId;
        this.events = events;
        this.rule = rule;
        this.amount = amount;
    }

    public long getUserId() {
        return userId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<? extends Event> getEvents() {
        return events;
    }

    public PointRule getRule() {
        return rule;
    }

    public double getAmount() {
        return amount;
    }

}
