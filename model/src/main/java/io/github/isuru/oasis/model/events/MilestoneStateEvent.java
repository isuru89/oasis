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
    private final Double nextValue;
    private final Long nextValueInt;

    public MilestoneStateEvent(long userId, Milestone milestone, double value, Double nextValue) {
        this(userId, milestone, value, Long.MIN_VALUE, nextValue, null);
    }

    public MilestoneStateEvent(long userId, Milestone milestone, long value, Long nextValue) {
        this(userId, milestone, Double.MIN_VALUE, value, null, nextValue);
    }

    private MilestoneStateEvent(long userId, Milestone milestone, double value, long value_i,
                                Double nextValue, Long nextValueInt) {
        this.userId = userId;
        this.milestone = milestone;
        this.value = value;
        this.valueInt = value_i;
        this.nextValue = nextValue;
        this.nextValueInt = nextValueInt;
    }

    public boolean isDouble() {
        return value != Double.MIN_VALUE;
    }

    public Double getNextValue() {
        return nextValue;
    }

    public Long getNextValueInt() {
        return nextValueInt;
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
