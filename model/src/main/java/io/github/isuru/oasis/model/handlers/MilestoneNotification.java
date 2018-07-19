package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class MilestoneNotification implements Serializable {

    private final long userId;
    private final int level;
    private final Event event;
    private final Milestone milestone;

    public MilestoneNotification(long userId, int level, Event event, Milestone milestone) {
        this.userId = userId;
        this.level = level;
        this.event = event;
        this.milestone = milestone;
    }

    public Event getEvent() {
        return event;
    }

    public long getUserId() {
        return userId;
    }

    public int getLevel() {
        return level;
    }

    public Milestone getMilestone() {
        return milestone;
    }
}
