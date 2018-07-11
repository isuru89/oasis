package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.rules.PointRule;

import java.io.Serializable;
import java.util.List;

public class PointNotification implements Serializable {

    private List<? extends Event> events;
    private PointRule rule;
    private double amount;

    public PointNotification(List<? extends Event> events, PointRule rule, double amount) {
        this.events = events;
        this.rule = rule;
        this.amount = amount;
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
