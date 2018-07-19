package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Milestone;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class MilestoneStateEvent implements Serializable {

    private final long userId;
    private final Milestone milestone;
    private final double value;
    private final long valueInt;

    public MilestoneStateEvent(long userId, Milestone milestone, double value) {
        this(userId, milestone, value, Long.MIN_VALUE);
    }

    public MilestoneStateEvent(long userId, Milestone milestone, long value) {
        this(userId, milestone, Double.MIN_VALUE, value);
    }

    private MilestoneStateEvent(long userId, Milestone milestone, double value, long value_i) {
        this.userId = userId;
        this.milestone = milestone;
        this.value = value;
        this.valueInt = value_i;
    }

    public long getUserId() {
        return userId;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public double getValue() {
        return value;
    }

    public long getValueInt() {
        return valueInt;
    }
}
